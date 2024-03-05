package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

import static com.brennaswitzer.cookbook.util.PlanTestUtils.renderTree;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
@WithAliceBobEve
public class PlanItemRepositoryTest {

    @Autowired
    private PlanItemRepository repo;

    @Autowired
    private PlanRepository planRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EntityManager entityManager;

    private User alice;

    @BeforeEach
    public void setUp() {
        alice = userRepo.getByName("Alice");
    }

    @Test
    public void findById() {
        Plan groceries = new Plan(alice, "Groceries");
        planRepo.save(groceries);

        PlanItem oj = new PlanItem("OJ").of(groceries);
        assertNull(oj.getId());
        oj = repo.saveAndFlush(oj); // because IDENTITY generation, the flush isn't needed, but it's good style
        Long id = oj.getId();
        assertNotNull(id);
        entityManager.clear(); // so it has to reload

        //noinspection OptionalGetWithoutIsPresent
        assertEquals(oj.getName(), repo.findById(id).get().getName());
    }

    @Test
    public void trashBin() {
        PlanItem plan = new Plan(alice, "The Plan");
        repo.save(plan);
        PlanItem a = new PlanItem("a").of(plan);
        repo.save(a);
        PlanItem b = new PlanItem("b").of(a);
        repo.save(b);
        PlanItem c = new PlanItem("c").of(b);
        repo.save(c);
        Function<Long, PlanItem> fetch = id -> {
            entityManager.flush();
            entityManager.clear();
            return repo.getReferenceById(id);
        };

        plan = fetch.apply(plan.getId());
        System.out.println(renderTree("initial", plan));

        fetch.apply(b.getId()).moveToTrash();
        plan = fetch.apply(plan.getId());
        System.out.println(renderTree("trash b", plan));

        fetch.apply(a.getId()).moveToTrash();
        plan = fetch.apply(plan.getId());
        System.out.println(renderTree("trash a", plan));

        fetch.apply(b.getId()).restoreFromTrash();
        plan = fetch.apply(plan.getId());
        System.out.println(renderTree("restore b", plan));

        fetch.apply(a.getId()).restoreFromTrash();
        plan = fetch.apply(plan.getId());
        System.out.println(renderTree("restore a", plan));
    }

}
