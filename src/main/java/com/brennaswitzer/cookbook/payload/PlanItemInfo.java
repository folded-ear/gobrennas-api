package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.brennaswitzer.cookbook.util.IdUtils.toIdList;

@SuppressWarnings("WeakerAccess")
public class PlanItemInfo {

    public static PlanItemInfo fromPlanItem(PlanItem item) {
        PlanItemInfo info = new PlanItemInfo();
        info.id = item.getId();
        info.name = item.getName();
        if (item.hasNotes()) {
            info.notes = item.getNotes();
        }
        info.status = item.getStatus();
        if (item.isSubtask()) {
            info.parentId = item.getParent().getId();
        }
        if (item.hasSubtasks()) {
            info.subtaskIds = toIdList(item.getOrderedSubtasksView());
        }
        if (item.isComponent()) {
            info.aggregateId = item.getAggregate().getId();
        }
        if (item.hasComponents()) {
            info.componentIds = toIdList(item.getOrderedComponentsView());
        }
        if (item.hasIngredient()) {
            info.ingredientId = item.getIngredient().getId();
            Quantity q = item.getQuantity();
            info.quantity = q.getQuantity();
            if (q.hasUnits()) {
                info.uomId = q.getUnits().getId();
                info.units = q.getUnits().getName();
            }
            info.preparation = item.getPreparation();
        }
        if (item.hasBucket()) {
            info.bucketId = item.getBucket().getId();
        }
        return info;
    }

    public static PlanItemInfo fromList(TaskList list) {
        return fromPlan(list);
    }

    public static PlanItemInfo fromPlan(TaskList plan) {
        PlanItemInfo info = fromPlanItem(plan);
        info.acl = AclInfo.fromAcl(plan.getAcl());
        if (plan.hasBuckets()) {
            info.buckets = plan.getBuckets().stream()
                    .map(PlanBucketInfo::from)
                    .collect(Collectors.toList());
        }
        return info;
    }

    public static List<PlanItemInfo> fromTasks(Iterable<PlanItem> tasks) {
        return StreamSupport.stream(tasks.spliterator(), false)
                .map(PlanItemInfo::fromPlanItem)
                .collect(Collectors.toList());
    }

    public static List<PlanItemInfo> fromLists(Iterable<TaskList> lists) {
        return fromPlans(lists);
    }

    public static List<PlanItemInfo> fromPlans(Iterable<TaskList> plans) {
        return StreamSupport.stream(plans.spliterator(), false)
                .map(PlanItemInfo::fromList)
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
    private String notes;

    @Getter
    @Setter
    private TaskStatus status;

    @Getter
    @Setter
    private Long parentId;

    @Getter
    @Setter
    private Long aggregateId;

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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long[] componentIds;

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
    private Long bucketId;

    @Getter
    @Setter
    private String preparation;

    public boolean hasSubtasks() {
        return subtaskIds != null && subtaskIds.length > 0;
    }

}
