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

@Setter
@Getter
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
        if (item.isChild()) {
            info.parentId = item.getParent().getId();
        }
        if (item.hasChildren()) {
            info.subtaskIds = toIdList(item.getOrderedChildView());
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

    public static PlanItemInfo fromPlan(Plan plan) {
        PlanItemInfo info = fromPlanItem(plan);
        info.acl = AclInfo.fromAcl(plan.getAcl());
        info.color = plan.getColor();
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

    private Long id;

    private String name;

    private String notes;

    private PlanItemStatus status;

    private Long parentId;

    private Long aggregateId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private AclInfo acl;

    /*
     The use of NON_EMPTY here is a complete kludge, so when a PlanItem-typed
     Plan is serialized, it won't carry a null color. The client's synchronizer
     ends up getting such plans due to Hibernate's non-polymorphic proxies over
     the PlanItem hierarchy. It's a mess.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String color;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PlanBucketInfo> buckets;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long[] subtaskIds;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private long[] componentIds;

    private Double quantity;

    private String units;

    private Long uomId;

    private Long ingredientId;

    private Long bucketId;

    private String preparation;

}
