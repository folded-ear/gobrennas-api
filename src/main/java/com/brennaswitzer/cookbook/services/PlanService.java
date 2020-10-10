package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.message.PlanMessage;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    protected UserPrincipalAccess principalAccess;

    @Autowired
    protected SimpMessagingTemplate messagingTemplate;

    protected Task getTaskById(Long id) {
        return getTaskById(id, AccessLevel.VIEW);
    }

    protected Task getTaskById(Long id, AccessLevel requiredAccess) {
        Task task = taskRepo.getOne(id);
        task.getTaskList().ensurePermitted(
                principalAccess.getUser(),
                requiredAccess
        );
        return task;
    }

    public List<Task> getTreeById(Long id) {
        return treeHelper(getTaskById(id, AccessLevel.VIEW));
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
            messagingTemplate.convertAndSend(
                    "/topic/plan/" + parent.getTaskList().getId(),
                    m);
        }
    }

    private boolean isMessagingCapable() {
        return messagingTemplate != null;
    }

    private void sendToPlan(AggregateIngredient r, Task rTask) {
        r.getIngredients().forEach(ir ->
                sendToPlan(ir, rTask));
    }

    private void sendToPlan(IngredientRef<?> ir, Task rTask) {
        Task t = new Task(ir.getRaw(), ir.getQuantity(), ir.getIngredient(), ir.getPreparation());
        rTask.addSubtask(t);
        if (ir.getIngredient() instanceof AggregateIngredient) {
            sendToPlan((AggregateIngredient) ir.getIngredient(), t);
        }
    }

    public void addRecipe(Long planId, Recipe r) {
        Task rTask = new Task(r.getName(), r);
        Task plan = getTaskById(planId, AccessLevel.CHANGE);
        plan.addSubtask(rTask);
        sendToPlan(r, rTask);
        if (isMessagingCapable()) {
            taskRepo.flush(); // so that IDs will be available to send out
            PlanMessage m = new PlanMessage();
            m.setId(planId);
            m.setType("create");
            List<Task> tree = new LinkedList<>();
            tree.add(plan);
            treeHelper(rTask, tree);
            m.setInfo(TaskInfo.fromTasks(tree));
            messagingTemplate.convertAndSend(
                    "/topic/plan/" + planId,
                    m);
        }
    }
}

