package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.brennaswitzer.cookbook.util.CollectionUtils.tail;

@Component
public class PlanItemResolver implements GraphQLResolver<PlanItem> {

    @Autowired
    private PlanService planService;

    public PlanItem parent(PlanItem item) {
        return item.getParent();
    }

    public List<PlanItem> children(PlanItem item) {
        return item.getOrderedChildView();
    }

    public List<PlanItem> components(PlanItem item) {
        return item.getOrderedComponentsView();
    }

    public int descendantCount(PlanItem item) {
        return planService.getTreeById(item).size() - 1;
    }

    public List<PlanItem> descendants(PlanItem item) {
        return tail(planService.getTreeById(item));
    }

}
