package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({
        "SpringJavaAutowiredFieldsWarningInspection",
        "UnusedReturnValue"})
@Service
@Transactional
public class TaskService {

    @Autowired
    private PlanItemRepository planItemRepo;

    @Autowired
    private PlanService planService;

    @Autowired
    private TaskListRepository listRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private ItemService itemService;

    public Iterable<Plan> getTaskLists() {
        return planService.getPlans();
    }

    public Iterable<Plan> getTaskLists(User owner) {
        return planService.getPlans(owner);
    }

    public Plan getTaskListById(Long id) {
        return planService.getPlanById(id, AccessLevel.VIEW);
    }

    public Plan createTaskList(String name, User user) {
        return planService.createTaskList(name, user);
    }

    public Plan duplicateTaskList(String name, Long fromId) {
        return planService.duplicateTaskList(name, fromId);
    }

    public Plan createTaskList(String name, Long userId) {
        return planService.createTaskList(name, userId);
    }

    public PlanItem createSubtask(Long parentId, String name) {
        PlanItem it = new PlanItem(name);
        itemService.autoRecognize(it);
        planService.getTaskById(parentId, AccessLevel.CHANGE)
                .addChildAfter(it, null);
        planItemRepo.save(it);
        return it;
    }

    public PlanItem createSubtaskAfter(Long parentId, String name, Long afterId) {
        if (afterId == null) {
            throw new IllegalArgumentException("You can't create a subtask after 'null'");
        }
        PlanItem it = new PlanItem(name);
        itemService.autoRecognize(it);
        planService.getTaskById(parentId, AccessLevel.CHANGE)
                .addChildAfter(it, planService.getTaskById(afterId));
        planItemRepo.save(it);
        return it;
    }

    public PlanItem renameTask(Long id, String name) {
        PlanItem it = planService.getTaskById(id, AccessLevel.CHANGE);
        it.setName(name);
        itemService.updateAutoRecognition(it);
        return it;
    }

    public PlanItem resetSubtasks(Long id, long[] subtaskIds) {
        PlanItem it = planService.getTaskById(id, AccessLevel.CHANGE);
        PlanItem prev = null;
        for (long sid : subtaskIds) {
            PlanItem curr = planService.getTaskById(sid);
            it.addChildAfter(curr, prev);
            prev = curr;
        }
        return it;
    }

    public void deleteTask(Long id) {
        deleteTask(planService.getTaskById(id, AccessLevel.CHANGE));
    }

    private void deleteTask(PlanItem it) {
        if (it.hasParent()) {
            it.getParent().removeChild(it);
        }
        planItemRepo.delete(it);
    }

    public Plan setGrantOnList(Long listId, Long userId, AccessLevel level) {
        Plan list = planService.getPlanById(listId, AccessLevel.ADMINISTER);
        list.getAcl().setGrant(userRepo.getById(userId), level);
        return list;
    }

    public Plan deleteGrantFromList(Long listId, Long userId) {
        Plan list = planService.getPlanById(listId, AccessLevel.ADMINISTER);
        list.getAcl().deleteGrant(userRepo.getById(userId));
        return list;
    }

}
