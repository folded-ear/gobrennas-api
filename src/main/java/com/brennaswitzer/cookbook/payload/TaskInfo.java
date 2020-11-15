package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("WeakerAccess")
public class TaskInfo {

    public static TaskInfo fromTask(Task task) {
        TaskInfo info = new TaskInfo();
        info.id = task.getId();
        info.name = task.getName();
        info.status = task.getStatus();
        if (task.isSubtask()) {
            info.parentId = task.getParent().getId();
        }
        if (task.hasSubtasks()) {
            info.subtaskIds = new long[task.getSubtaskCount()];
            int i = 0;
            for (Task t : task.getOrderedSubtasksView()) {
                info.subtaskIds[i++] = t.getId();
            }
        }
        if (task.hasIngredient()) {
            info.ingredientId = task.getIngredient().getId();
            Quantity q = task.getQuantity();
            info.quantity = q.getQuantity();
            if (q.hasUnits()) {
                info.uomId = q.getUnits().getId();
                info.units = q.getUnits().getName();
            }
            info.preparation = task.getPreparation();
        }
        return info;
    }

    public static TaskInfo fromList(TaskList list) {
        TaskInfo info = fromTask(list);
        info.acl = AclInfo.fromAcl(list.getAcl());
        if (list.hasBuckets()) {
            info.buckets = list.getBuckets().stream()
                    .map(PlanBucketInfo::from)
                    .collect(Collectors.toList());
        }
        return info;
    }

    public static List<TaskInfo> fromTasks(Iterable<Task> tasks) {
        return StreamSupport.stream(tasks.spliterator(), false)
                .map(TaskInfo::fromTask)
                .collect(Collectors.toList());
    }

    public static List<TaskInfo> fromLists(Iterable<TaskList> lists) {
        return StreamSupport.stream(lists.spliterator(), false)
                .map(TaskInfo::fromList)
                .collect(Collectors.toList());
    }

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private TaskStatus status;

    @Getter
    @Setter
    private Long parentId;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private AclInfo acl;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PlanBucketInfo> buckets;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long[] subtaskIds;

    @Getter
    @Setter
    private Double quantity;

    @Getter
    @Setter
    private String units;

    @Getter
    @Setter
    private Long uomId;

    @Getter
    @Setter
    private Long ingredientId;

    @Getter
    @Setter
    private String preparation;

    public boolean hasSubtasks() {
        return subtaskIds != null && subtaskIds.length > 0;
    }

}
