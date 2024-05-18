package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.brennaswitzer.cookbook.util.IdUtils.toIdList;
import static com.brennaswitzer.cookbook.util.IdUtils.toStringIdList;

@SuppressWarnings("WeakerAccess")
public class PlanItemInfo {

    public static PlanItemInfo fromPlanItem(PlanItem item) {
        PlanItemInfo info = new PlanItemInfo();
        info.id = item.getId().toString();
        info.name = item.getName();
        if (item.hasNotes()) {
            info.notes = item.getNotes();
        }
        info.status = item.getStatus();
        if (item.isChild()) {
            info.parentId = item.getParent().getId().toString();
        }
        if (item.hasChildren()) {
            info.subtaskIds = toStringIdList(item.getOrderedChildView());
        }
        if (item.isComponent()) {
            info.aggregateId = item.getAggregate().getId().toString();
        }
        if (item.hasComponents()) {
            info.componentIds = toStringIdList(item.getOrderedComponentsView());
        }
        if (item.hasIngredient()) {
            info.ingredientId = item.getIngredient().getId().toString();
            Quantity q = item.getQuantity();
            info.quantity = q.getQuantity();
            if (q.hasUnits()) {
                info.uomId = q.getUnits().getId().toString();
                info.units = q.getUnits().getName();
            }
            info.preparation = item.getPreparation();
        }
        if (item.hasBucket()) {
            info.bucketId = item.getBucket().getId().toString();
        }
        return info;
    }

    public static PlanItemInfo fromPlan(Plan plan) {
        PlanItemInfo info = fromPlanItem(plan);
        info.acl = AclInfo.fromAcl(plan.getAcl());
        if (plan.hasBuckets()) {
            info.buckets = plan.getBuckets().stream()
                    .map(PlanBucketInfo::from)
                    .collect(Collectors.toList());
        }
        return info;
    }

    public static List<PlanItemInfo> fromPlanItems(Iterable<PlanItem> items) {
        return StreamSupport.stream(items.spliterator(), false)
                .map(PlanItemInfo::fromPlanItem)
                .collect(Collectors.toList());
    }

    public static List<PlanItemInfo> fromPlans(Iterable<Plan> plans) {
        return StreamSupport.stream(plans.spliterator(), false)
                .map(PlanItemInfo::fromPlan)
                .collect(Collectors.toList());
    }

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String notes;

    @Getter
    @Setter
    private PlanItemStatus status;

    @Getter
    @Setter
    private String parentId;

    @Getter
    @Setter
    private String aggregateId;

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
    private String[] subtaskIds;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String[] componentIds;

    @Getter
    @Setter
    private Double quantity;

    @Getter
    @Setter
    private String units;

    @Getter
    @Setter
    private String uomId;

    @Getter
    @Setter
    private String ingredientId;

    @Getter
    @Setter
    private String bucketId;

    @Getter
    @Setter
    private String preparation;

    public boolean hasSubtasks() {
        return subtaskIds != null && subtaskIds.length > 0;
    }

}
