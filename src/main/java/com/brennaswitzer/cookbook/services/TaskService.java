package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
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
    private TaskRepository repo;

    public Iterable<Task> getRootTasks(User owner) {
        return repo.findByOwnerAndParentIsNull(owner);
    }

    public Task getTaskById(Long id) {
        return repo.getOne(id);
    }

    public Task createRootTask(String name, User user) {
        Task task = new Task(name);
        task.setOwner(user);
        task.setPosition(1 + repo.getMaxRootPosition(user));
        return repo.save(task);
    }

    public Task createSubtask(Long parentId, String name) {
        Task t = new Task(name);
        getTaskById(parentId)
            .insertSubtask(0, t);
        repo.save(t);
        return t;
    }

    public Task createSubtaskAfter(Long parentId, String name, Long afterId) {
        if (afterId == null) {
            throw new IllegalArgumentException("You can't create a subtas after 'null'");
        }
        Task t = new Task(name);
        getTaskById(parentId).addSubtaskAfter(t, getTaskById(afterId));
        repo.save(t);
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
        repo.deleteById(id);
    }

}
