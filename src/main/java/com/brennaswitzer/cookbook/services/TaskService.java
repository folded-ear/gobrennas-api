package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Iterable<Task> findRootTasks() {
        return taskRepository.findByParentIsNull();
    }
}
