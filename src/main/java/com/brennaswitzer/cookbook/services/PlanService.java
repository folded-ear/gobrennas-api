package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.message.PlanMessage;
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
        Task t = getTaskById(id, AccessLevel.VIEW);
        List<Task> result = new LinkedList<>();
        treeHelper(t, result);
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

}

