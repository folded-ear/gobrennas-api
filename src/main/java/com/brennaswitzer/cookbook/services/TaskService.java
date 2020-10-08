package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    private TaskRepository taskRepo;

    @Autowired
    private TaskListRepository listRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ItemService itemService;

    public Iterable<TaskList> getTaskLists(User owner) {
        return getTaskLists(owner.getId());
    }

    public Iterable<TaskList> getTaskLists() {
        return getTaskLists(principalAccess.getId());
    }

    public Iterable<TaskList> getTaskLists(Long userId) {
        User user = userRepo.getById(userId);
        List<TaskList> result = new LinkedList<>();
        listRepo.findAccessibleLists(userId)
                .forEach(l -> {
                    if (l.isPermitted(user, AccessLevel.VIEW)) {
                        result.add(l);
                    }
                });
        return result;
    }

    public Task getTaskById(Long id) {
        return getTaskById(id, AccessLevel.VIEW);
    }

    private Task getTaskById(Long id, AccessLevel requiredAccess) {
        Task task = taskRepo.getOne(id);
        task.getTaskList().ensurePermitted(
                principalAccess.getUser(),
                requiredAccess
        );
        return task;
    }

    public TaskList getTaskListById(Long id) {
        return getTaskListById(id, AccessLevel.VIEW);
    }

    private TaskList getTaskListById(Long id, AccessLevel accessLevel) {
        TaskList list = listRepo.getOne(id);
        list.ensurePermitted(
                principalAccess.getUser(),
                accessLevel
        );
        return list;
    }

    public TaskList createTaskList(String name, User user) {
        return createTaskList(name, user.getId());
    }

    public TaskList createTaskList(String name) {
        return createTaskList(name, principalAccess.getId());
    }

    public TaskList createTaskList(String name, Long userId) {
        User user = userRepo.getById(userId);
        TaskList list = new TaskList(name);
        list.setOwner(user);
        list.setPosition(1 + listRepo.getMaxPosition(user));
        return listRepo.save(list);
    }

    public Task createSubtask(Long parentId, String name) {
        Task t = new Task(name);
        itemService.autoRecognize(t);
        getTaskById(parentId, AccessLevel.CHANGE)
            .insertSubtask(0, t);
        taskRepo.save(t);
        return t;
    }

    public Task createSubtaskAfter(Long parentId, String name, Long afterId) {
        if (afterId == null) {
            throw new IllegalArgumentException("You can't create a subtask after 'null'");
        }
        Task t = new Task(name);
        itemService.autoRecognize(t);
        getTaskById(parentId, AccessLevel.CHANGE)
                .addSubtaskAfter(t, getTaskById(afterId));
        taskRepo.save(t);
        return t;
    }

    public void resetParent(Long id, Long parentId) {
        Task t = getTaskById(id);
        Task p = t.getParent();
        Task np = getTaskById(parentId);
        p.removeSubtask(t);
        if (np == p.getParent()) {
            np.addSubtaskAfter(t, p);
        } else {
            np.addSubtask(t);
        }
    }

    public Task renameTask(Long id, String name) {
        Task t = getTaskById(id, AccessLevel.CHANGE);
        t.setName(name);
        itemService.autoRecognize(t);
        return t;
    }

    public Task setStatus(Long id, TaskStatus status) {
        Task t = getTaskById(id, AccessLevel.CHANGE);
        if (TaskStatus.COMPLETED.equals(status) || TaskStatus.DELETED.equals(status)) {
            deleteTask(id);
        } else {
            t.setStatus(status);
        }
        return t;
    }

    public Task resetSubtasks(Long id, long[] subtaskIds) {
        Task t = getTaskById(id, AccessLevel.CHANGE);
        Task prev = null;
        for (long sid : subtaskIds) {
            Task curr = getTaskById(sid);
            t.addSubtaskAfter(curr, prev);
            prev = curr;
        }
        return t;
    }

    public void deleteTask(Long id) {
        deleteTask(getTaskById(id, AccessLevel.CHANGE));
    }

    private void deleteTask(Task t) {
        if (t.hasParent()) {
            t.getParent().removeSubtask(t);
        }
        taskRepo.delete(t);
    }

    public TaskList setGrantOnList(Long listId, Long userId, AccessLevel level) {
        TaskList list = getTaskListById(listId, AccessLevel.ADMINISTER);
        list.getAcl().setGrant(userRepo.getById(userId), level);
        return list;
    }

    public TaskList deleteGrantFromList(Long listId, Long userId) {
        TaskList list = getTaskListById(listId, AccessLevel.ADMINISTER);
        list.getAcl().deleteGrant(userRepo.getById(userId));
        return list;
    }

}
