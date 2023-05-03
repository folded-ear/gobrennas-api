package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.TaskStatus;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.message.PlanMessage;
import com.brennaswitzer.cookbook.payload.PlanBucketInfo;
import com.brennaswitzer.cookbook.payload.PlanItemInfo;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
@Transactional
public class PlanService {

    @Autowired
    protected PlanItemRepository taskRepo;

    @Autowired
    protected TaskListRepository planRepo;

    @Autowired
    protected PlanBucketRepository bucketRepo;

    @Autowired
    protected UserPrincipalAccess principalAccess;

    @Autowired
    private ItemService itemService;

    protected PlanItem getTaskById(Long id) {
        return getTaskById(id, AccessLevel.VIEW);
    }

    protected PlanItem getTaskById(Long id, AccessLevel requiredAccess) {
        PlanItem task = taskRepo.getReferenceById(id);
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

    public List<PlanItem> getTreeById(Long id) {
        return getTreeById(getTaskById(id, AccessLevel.VIEW));
    }

    public List<PlanItem> getTreeById(PlanItem task) {
        List<PlanItem> tasks = treeHelper(task);
        for (PlanItem t : tasks) {
            // to load the collection before the session closes
            t.getOrderedComponentsView();
        }
        return tasks;
    }

    private List<PlanItem> treeHelper(PlanItem task) {
        List<PlanItem> result = new LinkedList<>();
        treeHelper(task, result);
        return result;
    }

    private void treeHelper(PlanItem task, Collection<PlanItem> collector) {
        collector.add(task);
        if (task.hasSubtasks()) {
            task.getOrderedSubtasksView()
                    .forEach(t -> treeHelper(t, collector));
        }
    }

    public List<PlanItem> getTreeDeltasById(Long id, Instant cutoff) {
        val plan = getPlanById(id, AccessLevel.VIEW);
        return Stream.concat(
                        getTreeById(plan).stream(),
                        plan.getTrashBinTasks().stream()
                )
                .filter(t -> t.getUpdatedAt().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    public PlanMessage mutateTree(List<Long> ids, Long parentId, Long afterId) {
        PlanItem parent = getTaskById(parentId, AccessLevel.CHANGE);
        PlanItem after = afterId == null ? null : getTaskById(afterId, AccessLevel.VIEW);
        for (Long id : ids) {
            PlanItem t = getTaskById(id, AccessLevel.CHANGE);
            parent.addSubtaskAfter(t, after);
            after = t;
        }
        val m = new PlanMessage();
        m.setType("tree-mutation");
        m.setInfo(new MutatePlanTree(ids, parentId, afterId));
        return m;
    }

    public PlanMessage resetSubitems(Long id, List<Long> subitemIds) {
        PlanItem t = getTaskById(id, AccessLevel.CHANGE);
        PlanItem prev = null;
        for (Long sid : subitemIds) {
            PlanItem curr = getTaskById(sid);
            t.addSubtaskAfter(curr, prev);
            prev = curr;
        }
        return buildUpdateMessage(t);
    }

    private void sendToPlan(AggregateIngredient r, PlanItem aggTask, Double scale) {
        r.getIngredients().forEach(ir ->
                                           sendToPlan(ir, aggTask, scale));
    }

    private void sendToPlan(IngredientRef ref, PlanItem aggTask, Double scale) {
        if (scale == null || scale <= 0) { // nonsense!
            scale = 1d;
        }
        boolean isAggregate = ref.getIngredient() instanceof AggregateIngredient;
        if (ref.hasQuantity()) {
            ref = ref.scale(scale);
        }
        PlanItem t = new PlanItem(
                isAggregate
                        ? ref.getIngredient().getName()
                        : ref.getRaw(),
                ref.getQuantity(),
                ref.getIngredient(),
                ref.getPreparation());
        aggTask.addAggregateComponent(t);
        if (isAggregate) {
            // Subrecipes DO NOT get scaled; there's not a quantifiable
            // relationship to multiply across.
            sendToPlan((AggregateIngredient) ref.getIngredient(), t, 1d);
        }
    }

    public void addRecipe(Long planId, Recipe r) {
        addRecipe(planId, r, 1d);
    }

    public void addRecipe(Long planId, Recipe r, Double scale) {
        PlanItem recipeTask = new PlanItem(r.getName(), r);
        PlanItem plan = getTaskById(planId, AccessLevel.CHANGE);
        plan.addSubtask(recipeTask);
        sendToPlan(r, recipeTask, scale);
    }

    private PlanMessage buildCreationMessage(PlanItem task) {
        PlanItem parent = task.getParent();
        PlanMessage m = new PlanMessage();
        m.setId(parent.getId());
        m.setType("create");
        List<PlanItem> tree = getTreeById(parent);
        m.setInfo(PlanItemInfo.fromTasks(tree));
        return m;
    }

    public PlanMessage createItem(Object id, Long parentId, Long afterId, String name) {
        PlanItem parent = getTaskById(parentId, AccessLevel.CHANGE);
        PlanItem after = afterId == null ? null : getTaskById(afterId, AccessLevel.VIEW);
        PlanItem task = taskRepo.save(new PlanItem(name).of(parent, after));
        itemService.autoRecognize(task);
        if (task.getId() == null) taskRepo.flush();
        PlanMessage m = buildCreationMessage(task);
        m.addNewId(task.getId(), id);
        return m;
    }

    public PlanMessage createBucket(Long planId, Object bucketId, String name, LocalDate date) {
        TaskList plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = new PlanBucket();
        bucket.setName(name);
        bucket.setDate(date);
        bucket.setPlan(plan);
        bucket = bucketRepo.save(bucket);
        if (bucket.getId() != null) bucketRepo.flush();
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("create-bucket");
        m.setInfo(PlanBucketInfo.from(bucket));
        m.addNewId(bucket.getId(), bucketId);
        return m;
    }

    public PlanMessage updateBucket(Long planId, Long id, String name, LocalDate date) {
        TaskList plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        bucket.setName(name);
        bucket.setDate(date);
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("update-bucket");
        m.setInfo(PlanBucketInfo.from(bucket));
        return m;
    }

    public PlanMessage deleteBucket(Long planId, Long id) {
        TaskList plan = getPlanById(planId, AccessLevel.ADMINISTER);
        PlanBucket bucket = bucketRepo.getReferenceById(id);
        plan.getBuckets().remove(bucket);
        bucketRepo.delete(bucket);
        PlanMessage m = new PlanMessage();
        m.setId(bucket.getId());
        m.setType("delete-bucket");
        return m;
    }

    public PlanMessage renameItem(Long id, String name) {
        PlanItem task = getTaskById(id, AccessLevel.CHANGE);
        task.setName(name);
        if (!task.hasIngredient() || !(task.getIngredient() instanceof Recipe)) {
            itemService.updateAutoRecognition(task);
        }
        return buildUpdateMessage(task);
    }

    public PlanMessage assignItemBucket(Long id, Long bucketId) {
        PlanItem task = getTaskById(id, AccessLevel.CHANGE);
        task.setBucket(bucketId == null
                ? null
                : bucketRepo.getReferenceById(bucketId));
        return buildUpdateMessage(task);
    }

    private PlanMessage buildUpdateMessage(PlanItem task) {
        PlanMessage m = new PlanMessage();
        m.setId(task.getId());
        m.setType("update");
        m.setInfo(PlanItemInfo.fromPlanItem(task));
        return m;
    }

    public PlanMessage setItemStatus(Long id, TaskStatus status) {
        if (TaskStatus.COMPLETED.equals(status) || TaskStatus.DELETED.equals(status)) {
            return deleteItem(id);
        }
        PlanItem task = getTaskById(id, AccessLevel.CHANGE);
        task.setStatus(status);
        return buildUpdateMessage(task);
    }

    public PlanMessage deleteItem(Long id) {
        val task = getTaskById(id, AccessLevel.CHANGE);
        val plan = task.getTaskList();
        task.moveToTrash();
        val m = new PlanMessage();
        m.setId(id);
        m.setType("delete");
        return m;
    }

    public void severLibraryLinks(Recipe r) {
        taskRepo.findByIngredient(r).forEach(t -> {
            if (!t.hasNotes()) t.setNotes(r.getDirections());
            t.setIngredient(null);
        });
    }

}
