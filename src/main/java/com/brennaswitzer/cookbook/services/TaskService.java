package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
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
    private TaskRepository taskRepo;

    @Autowired
    private TaskListRepository listRepo;

    public Iterable<TaskList> getTaskLists(User owner) {
        return listRepo.findByOwner(owner);
    }

    public Task getTaskById(Long id) {
        return taskRepo.getOne(id);
    }

    public Task createTaskList(String name, User user) {
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

}
