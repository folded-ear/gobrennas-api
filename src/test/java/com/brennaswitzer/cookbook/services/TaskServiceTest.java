package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import com.brennaswitzer.cookbook.repositories.PlanRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.List;

import static com.brennaswitzer.cookbook.util.PlanTestUtils.renderTree;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class TaskServiceTest {

    @Autowired
    private TaskService service;

    @Autowired
    private PlanItemRepository itemRepo;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User alice, bob, eve;

    @BeforeEach
    public void setUp() {
        alice = userRepository.getByName("Alice");
        bob = userRepository.getByName("Bob");
        eve = userRepository.getByName("Eve");
    }

    @Test
    public void getPlans() {
        Iterator<Plan> itr = service.getPlans(alice).iterator();
        assertFalse(itr.hasNext());

        PlanItem groceries = itemRepo.save(new Plan(alice, "groceries"));

        itr = service.getPlans(alice).iterator();
        assertTrue(itr.hasNext());
        itr.next();
        assertFalse(itr.hasNext());

        PlanItem oj = itemRepo.save(new PlanItem("OJ")
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
    public void createChildItem() {
        Plan groceries = planRepo.save(new Plan(alice, "groceries"));
        assertEquals(0, groceries.getChildCount());

        PlanItem oj = service.createChildItem(groceries.getId(), "OJ");
        assertEquals("OJ", oj.getName());
        assertSame(groceries, oj.getParent());
        assertEquals(0, oj.getPosition());
        assertEquals(0, oj.getChildCount());

        PlanItem bagels = service.createChildItemAfter(groceries.getId(), "bagels", oj.getId());
        assertEquals("bagels", bagels.getName());
        assertSame(groceries, bagels.getParent());
        assertEquals(1, bagels.getPosition());
        assertEquals(0, bagels.getChildCount());

        PlanItem apples = service.createChildItem(groceries.getId(), "apples");
        assertEquals("apples", apples.getName());
        assertSame(groceries, apples.getParent());
        assertEquals(0, apples.getPosition());
        assertEquals(0, apples.getChildCount());

        assertEquals(0, apples.getPosition());
        assertEquals(1, oj.getPosition());
        assertEquals(2, bagels.getPosition());

        assertEquals(3, groceries.getChildCount());
        Iterator<PlanItem> itr = groceries.getOrderedChildView().iterator();
        assertSame(apples, itr.next());
        assertSame(oj, itr.next());
        assertSame(bagels, itr.next());
    }

    @Test
    public void renameItem() {
        Plan plan = planRepo.save(new Plan(alice, "root"));
        PlanItem bill = itemRepo.save(new PlanItem("bill").of(plan));

        service.renameItem(bill.getId(), "William");
        itemRepo.flush();
        entityManager.clear();

        bill = itemRepo.getReferenceById(bill.getId());
        assertEquals("William", bill.getName());
    }

    @Test
    public void resetChildItems() {
        Plan groceries = planRepo.save(new Plan(alice, "groceries"));
        PlanItem milk = itemRepo.save(new PlanItem("milk").of(groceries));
        PlanItem oj = itemRepo.save(new PlanItem("OJ").after(milk));
        PlanItem bagels = itemRepo.save(new PlanItem("bagels").after(oj));

        service.resetChildItems(groceries.getId(), new long[]{
                bagels.getId(),
                milk.getId(),
                oj.getId(),
        });
        itemRepo.flush();
        entityManager.clear();

        groceries = planRepo.getReferenceById(groceries.getId());
        List<PlanItem> view = groceries.getOrderedChildView();
        Iterator<PlanItem> itr = view.iterator();
        assertEquals("bagels", itr.next().getName());
        assertEquals("milk", itr.next().getName());
        assertEquals("OJ", itr.next().getName());
        itr = view.iterator();
        assertEquals(0, itr.next().getPosition());
        assertEquals(1, itr.next().getPosition());
        assertEquals(2, itr.next().getPosition());
    }

    @Test
    public void deleteItem() {
        assertEquals(0, itemRepo.count());
        Plan groceries = planRepo.save(new Plan(alice, "groceries"));
        PlanItem milk = itemRepo.save(new PlanItem("milk").of(groceries));
        PlanItem oj = itemRepo.save(new PlanItem("OJ").after(milk));
        PlanItem bagels = itemRepo.save(new PlanItem("bagels").after(oj));
        itemRepo.flush();
        entityManager.clear();

        assertEquals(4, itemRepo.count());

        service.deleteItem(oj.getId());
        itemRepo.flush();
        entityManager.clear();

        assertEquals(3, itemRepo.count());

        service.deleteItem(groceries.getId());
        itemRepo.flush();
        entityManager.clear();

        assertEquals(0, itemRepo.count());
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
    public void muppetLikePlansForShopping() {
        Plan groceries = service.createPlan("groceries", alice);
        PlanItem tacos = service.createChildItem(groceries.getId(), "Tacos");
        PlanItem salad = service.createChildItemAfter(groceries.getId(), "Salad", tacos.getId());
        PlanItem lunch = service.createChildItemAfter(groceries.getId(), "Lunch", salad.getId());

        muppetView("Meals");

        PlanItem meat = service.createChildItem(tacos.getId(), "meat");
        PlanItem tortillas = service.createChildItemAfter(tacos.getId(), "tortillas", meat.getId());
        PlanItem salsa = service.createChildItemAfter(tacos.getId(), "salsa", tortillas.getId());

        PlanItem lettuce = service.createChildItem(salad.getId(), "lettuce");
        PlanItem dressing = service.createChildItemAfter(salad.getId(), "dressing", lettuce.getId());
        PlanItem chicken = service.createChildItemAfter(salad.getId(), "chicken", dressing.getId());

        // oh, we need cheese too
        PlanItem cheese = service.createChildItem(tacos.getId(), "cheese");

        PlanItem ham = service.createChildItem(lunch.getId(), "deli ham");
        PlanItem cheese2 = service.createChildItemAfter(lunch.getId(), "cheese", ham.getId());
        PlanItem bread = service.createChildItemAfter(lunch.getId(), "bread", cheese2.getId());

        muppetView("Ingredients");

        PlanItem costco = service.createChildItem(groceries.getId(), "Costco");
        PlanItem winco = service.createChildItemAfter(groceries.getId(), "Winco", costco.getId());

        service.resetChildItems(winco.getId(), new long[]{
                meat.getId(),
        });
        service.resetChildItems(winco.getId(), new long[]{
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
        });
        service.resetChildItems(winco.getId(), new long[]{
                dressing.getId(),
                lettuce.getId(),
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
        });
        service.resetChildItems(winco.getId(), new long[]{
                dressing.getId(),
                lettuce.getId(),
                chicken.getId(),
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
        });
        service.resetChildItems(winco.getId(), new long[]{
                dressing.getId(),
                lettuce.getId(),
                ham.getId(),
                chicken.getId(),
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
                bread.getId(),
        });
        service.resetChildItems(costco.getId(), new long[]{
                cheese.getId(),
        });
        service.renameItem(cheese.getId(), "cheese (2)");
        service.deleteItem(cheese2.getId());

        muppetView("Shopping");
    }

    private void muppetView(String header) {
        itemRepo.flush();
        entityManager.clear();
        System.out.println(renderTree(header, service.getPlans(alice)));
    }

}
