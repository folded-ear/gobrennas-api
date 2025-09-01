package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

import static com.brennaswitzer.cookbook.util.CollectionUtils.tail;

@Controller
public class PlanItemResolver {

    @Autowired
    private PlanService planService;

    @SchemaMapping
    public Ingredient ingredient(PlanItem item) {
        return Hibernate.unproxy(item.getIngredient(), Ingredient.class);
    }

    @SchemaMapping
    public CorePlanItem parent(PlanItem item) {
        return item.getParent();
    }

    @SchemaMapping
    public List<PlanItem> children(PlanItem item) {
        return item.getOrderedChildView();
    }

    @SchemaMapping
    public List<PlanItem> components(PlanItem item) {
        return item.getOrderedComponentsView();
    }

    @SchemaMapping
    public int descendantCount(PlanItem item) {
        return planService.getTreeById(item).size() - 1;
    }

    @SchemaMapping
    public List<PlanItem> descendants(PlanItem item) {
        return tail(planService.getTreeById(item));
    }

}
