package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.message.PlanMessage;
import com.brennaswitzer.cookbook.payload.PlanBucketInfo;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
@Transactional
public class PlanService {

    @Autowired
    protected TaskRepository taskRepo;

    @Autowired
    protected TaskListRepository planRepo;

    @Autowired
    protected PlanBucketRepository bucketRepo;

    @Autowired
    protected UserPrincipalAccess principalAccess;

//    @Autowired
    protected SimpMessagingTemplate messagingTemplate; // todo: cull

    @Autowired
    private ItemService itemService;

    protected Task getTaskById(Long id) {
        return getTaskById(id, AccessLevel.VIEW);
    }

    protected Task getTaskById(Long id, AccessLevel requiredAccess) {
        Task task = taskRepo.getReferenceById(id);
        task.getTaskList().ensurePermitted(
                principalAccess.getUser(),
                requiredAccess
        );
        return task;
    }

    protected TaskList getPlanById(Long id, @SuppressWarnings("SameParameterValue") AccessLevel requiredAccess) {
        TaskList plan = planRepo.getReferenceById(id);
        plan.ensurePermitted(
                principalAccess.getUser(),
                requiredAccess
        );
        return plan;
    }

    public List<Task> getTreeById(Long id) {
        List<Task> tasks = treeHelper(getTaskById(id, AccessLevel.VIEW));
        for (Task t : tasks) {
            // to load the collection before the session closes
            t.getOrderedComponentsView();
        }
        return tasks;
    }

    private List<Task> treeHelper(Task task) {
        List<Task> result = new LinkedList<>();
        treeHelper(task, result);
        return result;
    }

    private void treeHelper(Task task, Collection<Task> collector) {
        collector.add(task);
        if (task.hasSubtasks()) {
            task.getOrderedSubtasksView()
                    .forEach(t -> treeHelper(t, collector));
        }
    }

    public void mutateTree(List<Long> ids, Long parentId, Long afterId) {
        Task parent = getTaskById(parentId, AccessLevel.CHANGE);
        Task after = afterId == null ? null : getTaskById(afterId, AccessLevel.VIEW);
        for (Long id : ids) {
            Task t = getTaskById(id, AccessLevel.CHANGE);
            parent.addSubtaskAfter(t, after);
            after = t;
        }
        if (isMessagingCapable()) {
            PlanMessage m = new PlanMessage();
            m.setType("tree-mutation");
            m.setInfo(new MutatePlanTree(ids, parentId, afterId));
            sendMessage(parent, m);
        }
    }

    public void resetSubitems(Long id, List<Long> subitemIds) {
        Task t = getTaskById(id, AccessLevel.CHANGE);
        Task prev = null;
        for (Long sid : subitemIds) {
            Task curr = getTaskById(sid);
            t.addSubtaskAfter(curr, prev);
            prev = curr;
        }
        if (isMessagingCapable()) {
            sendMessage(t, buildUpdateMessage(t));
        }
    }

    private boolean isMessagingCapable() { // todo: cull
        return messagingTemplate != null;
    }

    private void sendToPlan(AggregateIngredient r, Task aggTask) {
        r.getIngredients().forEach(ir ->
                sendToPlan(ir, aggTask));
    }

    private void sendToPlan(IngredientRef ir, Task aggTask) {
        Task t = new Task(ir.getRaw(), ir.getQuantity(), ir.getIngredient(), ir.getPreparation());
        aggTask.addAggregateComponent(t);
        if (ir.getIngredient() instanceof AggregateIngredient) {
            sendToPlan((AggregateIngredient) ir.getIngredient(), t);
        }
    }

    public void addRecipe(Long planId, Recipe r) {
        Task recipeTask = new Task(r.getName(), r);
        Task plan = getTaskById(planId, AccessLevel.CHANGE);
        plan.addSubtask(recipeTask);
        sendToPlan(r, recipeTask);
        if (isMessagingCapable()) {
            taskRepo.flush(); // so that IDs will be available
            sendMessage(plan, buildCreationMessage(recipeTask));
        }
    }

    private PlanMessage buildCreationMessage(Task task) {
        Task parent = task.getParent();
        PlanMessage m = new PlanMessage();
        m.setId(parent.getId());
        m.setType("create");
        List<Task> tree = new LinkedList<>();
        tree.add(parent);
        treeHelper(task, tree);
        m.setInfo(TaskInfo.fromTasks(tree));
        return m;
    }

    private void sendMessage(Task task, Object message) { // todo: cull
        if (!isMessagingCapable()) return;
        messagingTemplate.convertAndSend(
                "/topic/plan/" + task.getTaskList().getId(),
                message);
    }

    public void createItem(Object id, Long parentId, Long afterId, String name) {
        Task parent = getTaskById(parentId, AccessLevel.CHANGE);
        Task after = afterId == null ? null : getTaskById(afterId, AccessLevel.VIEW);
        Task task = taskRepo.save(new Task(name).of(parent, after));
        itemService.autoRecognize(task);
        if (isMessagingCapable()) {
            if (task.getId() == null) taskRepo.flush();
            PlanMessage m = buildCreationMessage(task);
            m.addNewId(task.getId(), id);
            sendMessage(parent, m);
        }
    }

    public void createBucket(Long planId, Object bucketId, String name, LocalDate date) {
        TaskList plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = new PlanBucket();
        bucket.setName(name);
        bucket.setDate(date);
        bucket.setPlan(plan);
        bucket = bucketRepo.save(bucket);
        if (isMessagingCapable()) {
            if (bucket.getId() != null) bucketRepo.flush();
            PlanMessage m = new PlanMessage();
            m.setId(bucket.getId());
            m.setType("create-bucket");
            m.setInfo(PlanBucketInfo.from(bucket));
            m.addNewId(bucket.getId(), bucketId);
            sendMessage(plan, m);
        }
    }

    public void updateBucket(Long planId, Long id, String name, LocalDate date) {
        TaskList plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        bucket.setName(name);
        bucket.setDate(date);
        if (isMessagingCapable()) {
            PlanMessage m = new PlanMessage();
            m.setId(bucket.getId());
            m.setType("update-bucket");
            m.setInfo(PlanBucketInfo.from(bucket));
            sendMessage(plan, m);
        }
    }

    public void deleteBucket(Long planId, Long id) {
        TaskList plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        plan.getBuckets().remove(bucket);
        bucketRepo.delete(bucket);
        if (isMessagingCapable()) {
            PlanMessage m = new PlanMessage();
            m.setId(bucket.getId());
            m.setType("delete-bucket");
            sendMessage(plan, m);
        }
    }

    public void renameItem(Long id, String name) {
        Task task = getTaskById(id, AccessLevel.CHANGE);
        task.setName(name);
        if (!task.hasIngredient() || !(task.getIngredient() instanceof Recipe)) {
            itemService.updateAutoRecognition(task);
        }
        if (isMessagingCapable()) {
            sendMessage(task, buildUpdateMessage(task));
        }
    }

    public void assignItemBucket(Long id, Long bucketId) {
        Task task = getTaskById(id, AccessLevel.CHANGE);
        task.setBucket(bucketId == null
                ? null
                : bucketRepo.getReferenceById(bucketId));
        if (isMessagingCapable()) {
            sendMessage(task, buildUpdateMessage(task));
        }
    }

    private PlanMessage buildUpdateMessage(Task task) {
        PlanMessage m = new PlanMessage();
        m.setId(task.getId());
        m.setType("update");
        m.setInfo(TaskInfo.fromTask(task));
        return m;
    }

    public void setItemStatus(Long id, TaskStatus status) {
        if (TaskStatus.COMPLETED.equals(status) || TaskStatus.DELETED.equals(status)) {
            deleteItem(id);
            return;
        }
        Task task = getTaskById(id, AccessLevel.CHANGE);
        task.setStatus(status);
        if (isMessagingCapable()) {
            sendMessage(task, buildUpdateMessage(task));
        }
    }

    public void deleteItem(Long id) {
        val task = getTaskById(id, AccessLevel.CHANGE);
        val plan = task.getTaskList();
        task.moveToTrash();
        if (isMessagingCapable()) {
            PlanMessage m = new PlanMessage();
            m.setId(id);
            m.setType("delete");
            sendMessage(plan, m);
        }
    }

    public void severLibraryLinks(Recipe r) {
        taskRepo.findByIngredient(r).forEach(t -> {
            if (!t.hasNotes()) t.setNotes(r.getDirections());
            t.setIngredient(null);
            if (isMessagingCapable()) {
                sendMessage(t.getTaskList(), buildUpdateMessage(t));
            }
        });
    }

}

