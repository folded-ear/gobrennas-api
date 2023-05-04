package com.brennaswitzer.cookbook.services;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private UserRepository userRepo;

    private User alice;

    @BeforeEach
    public void setUp() {
        alice = userRepo.getByName("Alice");
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

}
