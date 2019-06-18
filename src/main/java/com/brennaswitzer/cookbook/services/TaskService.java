package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Permission;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
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
    private TaskRepository taskRepo;

    @Autowired
    private TaskListRepository listRepo;

    @Autowired
    private UserRepository userRepo;

    public Iterable<TaskList> getTaskLists(User owner) {
        return getTaskLists(owner.getId());
    }

    public Iterable<TaskList> getTaskLists(Long ownerId) {
        return listRepo.findByOwnerId(ownerId);
    }

    public Task getTaskById(Long id) {
        return taskRepo.getOne(id);
    }

    public TaskList getTaskListById(Long id) {
        return listRepo.getOne(id);
    }

    public TaskList createTaskList(String name, User user) {
        return createTaskList(name, user.getId());
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
        getTaskById(parentId)
            .insertSubtask(0, t);
        taskRepo.save(t);
        return t;
    }

    public Task createSubtaskAfter(Long parentId, String name, Long afterId) {
        if (afterId == null) {
            throw new IllegalArgumentException("You can't create a subtask after 'null'");
        }
        Task t = new Task(name);
        getTaskById(parentId).addSubtaskAfter(t, getTaskById(afterId));
        taskRepo.save(t);
        return t;
    }

    public Task renameTask(Long id, String name) {
        Task t = getTaskById(id);
        t.setName(name);
        return t;
    }

    public Task resetSubtasks(Long id, long[] subtaskIds) {
        Task t = getTaskById(id);
        Task prev = null;
        for (long sid : subtaskIds) {
            Task curr = getTaskById(sid);
            t.addSubtaskAfter(curr, prev);
            prev = curr;
        }
        return t;
    }

    public void deleteTask(Long id) {
        taskRepo.deleteById(id);
    }

    public TaskList setGrantOnList(Long listId, Long userId, Permission perm) {
        TaskList list = getTaskListById(listId);
        list.getAcl().setGrant(userRepo.getById(userId), perm);
        return list;
    }

    public TaskList deleteGrantFromList(Long listId, Long userId) {
        TaskList list = getTaskListById(listId);
        list.getAcl().deleteGrant(userRepo.getById(userId));
        return list;
    }

}
