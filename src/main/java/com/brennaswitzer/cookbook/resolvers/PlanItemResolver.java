package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.services.PlanService;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@Component
public class PlanItemResolver implements GraphQLResolver<PlanItem> {

    @Autowired
    private PlanService planService;

    public Double getQuantity(PlanItem item) {
        return item.getQuantity().getQuantity();
    }

    public String getUnits(PlanItem item) {
        Quantity q = item.getQuantity();
        if (q.hasUnits()) {
            return q.getUnits().getName();
        } else {
            return null;
        }
    }

    public List<PlanItem> descendants(PlanItem item) {
        return planService.getTreeById(item);
    }

    public List<PlanItem> children(PlanItem item) {
        return item.getOrderedChildView();
    }

    @Deprecated
    public Collection<PlanItem> subtasks(PlanItem item) {
        return children(item);
    }

    public List<PlanItem> components(PlanItem item) {
        return item.getOrderedComponentsView();
    }

}
