package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class PlanServiceTest {

    @Autowired
    private PlanService service;

    @Autowired
    private PlanItemRepository itemRepo;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private PlannedRecipeHistoryRepository recipeHistoryRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    UserPrincipalAccess principalAccess;

    @Autowired
    private UserRepository userRepo;

    private User alice, bob, eve;

    @BeforeEach
    public void setUp() {
        alice = userRepo.getByName("Alice");
        bob = userRepo.getByName("Bob");
        eve = userRepo.getByName("Eve");
    }

    @Test
    public void getPlans() {
        Iterator<Plan> itr = service.getPlans(alice.getId()).iterator();
        assertFalse(itr.hasNext());

        PlanItem groceries = itemRepo.save(new Plan(alice, "groceries"));

        itr = service.getPlans(alice).iterator();
        assertTrue(itr.hasNext());
        itr.next();
        assertFalse(itr.hasNext());

        itemRepo.save(new PlanItem("OJ")
                              .of(groceries));

        // still only one!
        itr = service.getPlans(alice).iterator();
        assertTrue(itr.hasNext());
        itr.next();
        assertFalse(itr.hasNext());
    }

    @Test
    public void createPlan() {
        PlanItem g = service.createPlan("Groceries", alice);
        assertNotNull(g.getId());
        assertEquals("Groceries", g.getName());
        assertEquals(0, g.getPosition());
        assertEquals(0, g.getChildCount());

        PlanItem v = service.createPlan("Vacation", alice);
        assertNotNull(v.getId());
        assertNotEquals(g.getId(), v.getId());
        assertEquals("Vacation", v.getName());
        assertEquals(1, v.getPosition());
        assertEquals(0, v.getChildCount());
    }

    @Test
    public void grants() {
        Plan groceries = service.createPlan("groceries", alice);
        itemRepo.flush();
        entityManager.clear();

        service.setGrantOnPlan(groceries.getId(), bob.getId(), AccessLevel.VIEW);

        groceries = service.getPlanById(groceries.getId());
        assertEquals(AccessLevel.ADMINISTER, groceries.getAcl().getGrant(alice));
        assertEquals(AccessLevel.VIEW, groceries.getAcl().getGrant(bob));
        assertNull(groceries.getAcl().getGrant(eve));

        entityManager.flush();
        entityManager.clear();
        groceries = service.getPlanById(groceries.getId());

        service.deleteGrantFromPlan(groceries.getId(), bob.getId());
        assertNull(groceries.getAcl().getGrant(bob));

        entityManager.flush();
        entityManager.clear();
        groceries = service.getPlanById(groceries.getId());

        assertNull(groceries.getAcl().getGrant(bob));
    }

    @Test
    public void renameItem() {
        Plan plan = planRepo.save(new Plan(alice, "root"));
        PlanItem bill = itemRepo.save(new PlanItem("bill").of(plan));

        service.renameItemForMessage(bill.getId(), "William");
        itemRepo.flush();
        entityManager.clear();

        bill = itemRepo.getReferenceById(bill.getId());
        assertEquals("William", bill.getName());
    }

    @Test
    public void deleteItem() {
        assertEquals(0, itemRepo.count());
        Plan groceries = planRepo.save(new Plan(alice, "groceries"));
        PlanItem milk = itemRepo.save(new PlanItem("milk").of(groceries));
        PlanItem oj = itemRepo.save(new PlanItem("OJ").after(milk));
        itemRepo.save(new PlanItem("bagels").after(oj));
        itemRepo.flush();
        entityManager.clear();

        assertEquals(4, itemRepo.count());

        service.deleteItemForParent(oj.getId());
        itemRepo.flush();
        entityManager.clear();

        assertEquals(4, itemRepo.count());
        assertEquals(3, itemRepo.countByStatusNot(PlanItemStatus.DELETED));

        service.deletePlan(groceries.getId());
        itemRepo.flush();
        entityManager.clear();

        assertEquals(0, itemRepo.count());
    }

    @Test
    void statusChangeEvents() {
        assert 0 == recipeHistoryRepo.count(); // sanity
        var box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        Plan groceries = service.createPlan("This Week", alice);
        service.addRecipe(groceries.getId(), box.pizza, 1.0);
        entityManager.flush();
        PlanItem pizza = groceries.getChildView().iterator().next();
        PlanItem sauce = null, crust = null;
        for (var it : pizza.getChildView()) {
            if (box.pizzaSauce.equals(it.getIngredient())) {
                sauce = it;
                // mark tomatoes acquired
                service.setItemStatusForMessage(it.getChildView().iterator().next().getId(),
                                                PlanItemStatus.ACQUIRED);
                entityManager.flush();
            } else if (box.pizzaCrust.equals(it.getIngredient())) {
                crust = it;
                // delete crust
                service.deleteItemForParent(it.getId());
                entityManager.flush();
            }
        }
        assert sauce != null;
        assert crust != null;
        // mark pizza (and implicitly, sauce) completed
        service.setItemStatus(pizza.getId(),
                              PlanItemStatus.COMPLETED);
        entityManager.flush();

        var byRecipe = recipeHistoryRepo.findAll()
                .stream()
                .collect(Collectors.toMap(PlannedRecipeHistory::getRecipe,
                                          Function.identity()));
        // crust got deleted
        var h = byRecipe.get(box.pizzaCrust);
        assertEquals(crust.getId(), h.getPlanItemId());
        assertNotNull(h.getPlannedAt());
        assertEquals(PlanItemStatus.DELETED, h.getStatus());
        // pizza got completed
        h = byRecipe.get(box.pizza);
        assertEquals(pizza.getId(), h.getPlanItemId());
        assertNotNull(h.getPlannedAt());
        assertEquals(PlanItemStatus.COMPLETED, h.getStatus());
        // sauce got (implicitly) completed
        h = byRecipe.get(box.pizzaSauce);
        assertEquals(sauce.getId(), h.getPlanItemId());
        assertNotNull(h.getPlannedAt());
        assertEquals(PlanItemStatus.COMPLETED, h.getStatus());
        // ignore tomatoes - not a recipe
        assertEquals(3, recipeHistoryRepo.count());
    }

}
