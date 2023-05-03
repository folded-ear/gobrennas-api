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

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({
        "SpringJavaAutowiredFieldsWarningInspection",
        "UnusedReturnValue"})
@Service
@Transactional
public class TaskService {

    @Autowired
    private PlanItemRepository planItemRepo;

    @Autowired
    private TaskListRepository listRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private ItemService itemService;

    public Iterable<Plan> getTaskLists(User owner) {
        return getTaskLists(owner.getId());
    }

    public Iterable<Plan> getTaskLists() {
        return getTaskLists(principalAccess.getId());
    }

    public Iterable<Plan> getTaskLists(Long userId) {
        User user = userRepo.getById(userId);
        List<Plan> result = new LinkedList<>();
        listRepo.findAccessibleLists(userId)
                .forEach(l -> {
                    if (l.isPermitted(user, AccessLevel.VIEW)) {
                        result.add(l);
                    }
                });
        return result;
    }

    public PlanItem getTaskById(Long id) {
        return getTaskById(id, AccessLevel.VIEW);
    }

    private PlanItem getTaskById(Long id, AccessLevel requiredAccess) {
        PlanItem item = planItemRepo.getReferenceById(id);
        item.getTaskList().ensurePermitted(
                principalAccess.getUser(),
                requiredAccess
        );
        return item;
    }

    public Plan getTaskListById(Long id) {
        return getTaskListById(id, AccessLevel.VIEW);
    }

    private Plan getTaskListById(Long id, AccessLevel accessLevel) {
        Plan list = listRepo.getReferenceById(id);
        list.ensurePermitted(
                principalAccess.getUser(),
                accessLevel
        );
        return list;
    }

    public Plan createTaskList(String name, User user) {
        return createTaskList(name, user.getId());
    }

    public Plan duplicateTaskList(String name, Long fromId) {
        Plan list = createTaskList(name);
        Plan src = listRepo.getReferenceById(fromId);
        duplicateChildren(src, list);
        return list;
    }

    private void duplicateChildren(PlanItem src, PlanItem dest) {
        if (!src.hasSubtasks()) return;
        for (PlanItem s : src.getOrderedSubtasksView()) {
            PlanItem d = new PlanItem(
                    s.getName(),
                    s.getQuantity(),
                    s.getIngredient(),
                    s.getPreparation()
            );
            d.setStatus(s.getStatus()); // unclear if this is good?
            dest.addSubtask(d);
            duplicateChildren(s, d);
        }
    }

    public Plan createTaskList(String name) {
        return createTaskList(name, principalAccess.getId());
    }

    public Plan createTaskList(String name, Long userId) {
        User user = userRepo.getById(userId);
        Plan list = new Plan(name);
        list.setOwner(user);
        list.setPosition(1 + listRepo.getMaxPosition(user));
        return listRepo.save(list);
    }

    public PlanItem createSubtask(Long parentId, String name) {
        PlanItem it = new PlanItem(name);
        itemService.autoRecognize(it);
        getTaskById(parentId, AccessLevel.CHANGE)
                .addSubtaskAfter(it, null);
        planItemRepo.save(it);
        return it;
    }

    public PlanItem createSubtaskAfter(Long parentId, String name, Long afterId) {
        if (afterId == null) {
            throw new IllegalArgumentException("You can't create a subtask after 'null'");
        }
        PlanItem it = new PlanItem(name);
        itemService.autoRecognize(it);
        getTaskById(parentId, AccessLevel.CHANGE)
                .addSubtaskAfter(it, getTaskById(afterId));
        planItemRepo.save(it);
        return it;
    }

    public PlanItem renameTask(Long id, String name) {
        PlanItem it = getTaskById(id, AccessLevel.CHANGE);
        it.setName(name);
        itemService.updateAutoRecognition(it);
        return it;
    }

    public PlanItem resetSubtasks(Long id, long[] subtaskIds) {
        PlanItem it = getTaskById(id, AccessLevel.CHANGE);
        PlanItem prev = null;
        for (long sid : subtaskIds) {
            PlanItem curr = getTaskById(sid);
            it.addSubtaskAfter(curr, prev);
            prev = curr;
        }
        return it;
    }

    public void deleteTask(Long id) {
        deleteTask(getTaskById(id, AccessLevel.CHANGE));
    }

    private void deleteTask(PlanItem it) {
        if (it.hasParent()) {
            it.getParent().removeSubtask(it);
        }
        planItemRepo.delete(it);
    }

    public Plan setGrantOnList(Long listId, Long userId, AccessLevel level) {
        Plan list = getTaskListById(listId, AccessLevel.ADMINISTER);
        list.getAcl().setGrant(userRepo.getById(userId), level);
        return list;
    }

    public Plan deleteGrantFromList(Long listId, Long userId) {
        Plan list = getTaskListById(listId, AccessLevel.ADMINISTER);
        list.getAcl().deleteGrant(userRepo.getById(userId));
        return list;
    }

}
