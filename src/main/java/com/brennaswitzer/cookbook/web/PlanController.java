package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@MessageMapping("/plan")
@PreAuthorize("hasRole('USER')")
public class PlanController {

    @Autowired
    private PlanService planService;

    @SubscribeMapping("/{id}")
    public List<TaskInfo> subscribeToList(@DestinationVariable("id") Long id) {
        // Packaging up the entire tree into a single message is sorta less than
        // ideal. Sending only the top-level items w/ a follow-up message
        // containing the rest would give better perceived performance, as the
        // visible tasks would show up w/out having to wait for processing all
        // the nested subtasks, which are likely _much_ more numerous.
        //
        // I think?
        return TaskInfo.fromTasks(planService.getTreeById(id));
    }

    @MessageMapping("/{id}/mutate-tree")
    public void mutateTree(@Payload MutatePlanTree action) {
        planService.mutateTree(action.getIds(), action.getParentId(), action.getAfterId());
    }

}
