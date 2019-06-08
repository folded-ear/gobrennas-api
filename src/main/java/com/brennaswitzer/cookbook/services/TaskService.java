package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
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

    public Iterable<Task> getRootTasks() {
        return repo.findByParentIsNull();
    }

    public Task getTaskById(Long id) {
        return repo.getOne(id);
    }

    public Task createRootTask(String name) {
        Task task = new Task(name);
        task.setPosition(1 + repo.getMaxRootPosition());
        return repo.save(task);
    }

    public Task createSubtask(Long parentId, String name) {
        Task t = repo.save(new Task(name));
        getTaskById(parentId)
            .insertSubtask(0, t);
        return t;
    }

    public Task createSubtaskAfter(Long parentId, String name, Long afterId) {
        if (afterId == null) {
            throw new IllegalArgumentException("You can't create a subtas after 'null'");
        }
        Task t = repo.save(new Task(name));
        getTaskById(parentId).addSubtaskAfter(t, getTaskById(afterId));
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
