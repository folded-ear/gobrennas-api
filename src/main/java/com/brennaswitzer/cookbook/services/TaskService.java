package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({
        "SpringJavaAutowiredFieldsWarningInspection",
        "UnusedReturnValue"})
@Service
@Transactional
public class TaskService {

    @Autowired
    private PlanItemRepository planItemRepo;

    @Autowired
    private PlanService planService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ItemService itemService;

    public Iterable<Plan> getPlans(User owner) {
        return planService.getPlans(owner);
    }

    public Plan getPlanById(Long id) {
        return planService.getPlanById(id, AccessLevel.VIEW);
    }

    public Plan createPlan(String name, User user) {
        return planService.createPlan(name, user);
    }

    public PlanItem renameItem(Long id, String name) {
        PlanItem it = planService.getPlanItemById(id, AccessLevel.CHANGE);
        it.setName(name);
        itemService.updateAutoRecognition(it);
        return it;
    }

    public void deleteItem(Long id) {
        deleteItem(planService.getPlanItemById(id, AccessLevel.CHANGE));
    }

    private void deleteItem(PlanItem it) {
        if (it.hasParent()) {
            it.getParent().removeChild(it);
        }
        planItemRepo.delete(it);
    }

    public Plan setGrantOnPlan(Long planId, Long userId, AccessLevel level) {
        Plan plan = planService.getPlanById(planId, AccessLevel.ADMINISTER);
        plan.getAcl().setGrant(userRepo.getById(userId), level);
        return plan;
    }

    public Plan deleteGrantFromPlan(Long planId, Long userId) {
        Plan plan = planService.getPlanById(planId, AccessLevel.ADMINISTER);
        plan.getAcl().deleteGrant(userRepo.getById(userId));
        return plan;
    }

}
