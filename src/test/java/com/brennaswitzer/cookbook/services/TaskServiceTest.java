package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
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

import static com.brennaswitzer.cookbook.util.TaskTestUtils.renderTree;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class TaskServiceTest {

    @Autowired
    private TaskService service;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private TaskListRepository listRepo;

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
    public void getTaskLists() {
        Iterator<TaskList> itr = service.getTaskLists(alice.getId()).iterator();
        assertFalse(itr.hasNext());

        Task groceries = taskRepo.save(new TaskList(alice, "groceries"));

        itr = service.getTaskLists(alice).iterator();
        assertTrue(itr.hasNext());
        itr.next();
        assertFalse(itr.hasNext());

        Task oj = taskRepo.save(new Task("OJ")
                .of(groceries));

        // still only one!
        itr = service.getTaskLists(alice).iterator();
        assertTrue(itr.hasNext());
        itr.next();
        assertFalse(itr.hasNext());
    }

    @Test
    public void createTaskList() {

        Task g = service.createTaskList("Groceries", alice);
        assertNotNull(g.getId());
        assertEquals("Groceries", g.getName());
        assertEquals(0, g.getPosition());
        assertEquals(0, g.getSubtaskCount());

        Task v = service.createTaskList("Vacation", alice);
        assertNotNull(v.getId());
        assertNotEquals(g.getId(), v.getId());
        assertEquals("Vacation", v.getName());
        assertEquals(1, v.getPosition());
        assertEquals(0, v.getSubtaskCount());
    }

    @Test
    public void createSubtask() {
        TaskList groceries = listRepo.save(new TaskList(alice,"groceries"));
        assertEquals(0, groceries.getSubtaskCount());

        Task oj = service.createSubtask(groceries.getId(), "OJ");
        assertEquals("OJ", oj.getName());
        assertSame(groceries, oj.getParent());
        assertEquals(0, oj.getPosition());
        assertEquals(0, oj.getSubtaskCount());

        Task bagels = service.createSubtaskAfter(groceries.getId(), "bagels", oj.getId());
        assertEquals("bagels", bagels.getName());
        assertSame(groceries, bagels.getParent());
        assertEquals(1, bagels.getPosition());
        assertEquals(0, bagels.getSubtaskCount());

        Task apples = service.createSubtask(groceries.getId(), "apples");
        assertEquals("apples", apples.getName());
        assertSame(groceries, apples.getParent());
        assertEquals(0, apples.getPosition());
        assertEquals(0, apples.getSubtaskCount());

        assertEquals(0, apples.getPosition());
        assertEquals(1, oj.getPosition());
        assertEquals(2, bagels.getPosition());

        assertEquals(3, groceries.getSubtaskCount());
        Iterator<Task> itr = groceries.getOrderedSubtasksView().iterator();
        assertSame(apples, itr.next());
        assertSame(oj, itr.next());
        assertSame(bagels, itr.next());
    }

    @Test
    public void renameTask() {
        TaskList list = listRepo.save(new TaskList(alice, "root"));
        Task bill = taskRepo.save(new Task("bill").of(list));

        service.renameTask(bill.getId(), "William");
        taskRepo.flush();
        entityManager.clear();

        bill = taskRepo.getOne(bill.getId());
        assertEquals("William", bill.getName());
    }

    @Test
    public void resetSubtasks() {
        TaskList groceries = listRepo.save(new TaskList(alice, "groceries"));
        Task milk = taskRepo.save(new Task("milk").of(groceries));
        Task oj = taskRepo.save(new Task("OJ").after(milk));
        Task bagels = taskRepo.save(new Task("bagels").after(oj));

        service.resetSubtasks(groceries.getId(), new long[] {
                bagels.getId(),
                milk.getId(),
                oj.getId(),
        });
        taskRepo.flush();
        entityManager.clear();

        groceries = listRepo.getOne(groceries.getId());
        List<Task> view = groceries.getOrderedSubtasksView();
        Iterator<Task> itr = view.iterator();
        assertEquals("bagels", itr.next().getName());
        assertEquals("milk", itr.next().getName());
        assertEquals("OJ", itr.next().getName());
        itr = view.iterator();
        assertEquals(0, itr.next().getPosition());
        assertEquals(1, itr.next().getPosition());
        assertEquals(2, itr.next().getPosition());
    }

    @Test
    public void deleteTask() {
        assertEquals(0, taskRepo.count());
        TaskList groceries = listRepo.save(new TaskList(alice, "groceries"));
        Task milk = taskRepo.save(new Task("milk").of(groceries));
        Task oj = taskRepo.save(new Task("OJ").after(milk));
        Task bagels = taskRepo.save(new Task("bagels").after(oj));
        taskRepo.flush();
        entityManager.clear();

        assertEquals(4, taskRepo.count());

        service.deleteTask(oj.getId());
        taskRepo.flush();
        entityManager.clear();

        assertEquals(3, taskRepo.count());

        service.deleteTask(groceries.getId());
        taskRepo.flush();
        entityManager.clear();

        assertEquals(0, taskRepo.count());
    }

    @Test
    public void grants() {
        TaskList groceries = service.createTaskList("groceries", alice);
        taskRepo.flush();
        entityManager.clear();


        service.setGrantOnList(groceries.getId(), bob.getId(), AccessLevel.VIEW);

        groceries = service.getTaskListById(groceries.getId());
        assertEquals(AccessLevel.ADMINISTER, groceries.getAcl().getGrant(alice));
        assertEquals(AccessLevel.VIEW, groceries.getAcl().getGrant(bob));
        assertNull(groceries.getAcl().getGrant(eve));

        entityManager.flush();
        entityManager.clear();
        groceries = service.getTaskListById(groceries.getId());

        service.deleteGrantFromList(groceries.getId(), bob.getId());
        assertNull(groceries.getAcl().getGrant(bob));

        entityManager.flush();
        entityManager.clear();
        groceries = service.getTaskListById(groceries.getId());

        assertNull(groceries.getAcl().getGrant(bob));
    }

    @Test
    public void muppetLikeListsForShopping() {
        TaskList groceries = service.createTaskList("groceries", alice);
        Task tacos = service.createSubtask(groceries.getId(), "Tacos");
        Task salad = service.createSubtaskAfter(groceries.getId(), "Salad", tacos.getId());
        Task lunch = service.createSubtaskAfter(groceries.getId(), "Lunch", salad.getId());

        muppetView("Meals");

        Task meat = service.createSubtask(tacos.getId(), "meat");
        Task tortillas = service.createSubtaskAfter(tacos.getId(), "tortillas", meat.getId());
        Task salsa = service.createSubtaskAfter(tacos.getId(), "salsa", tortillas.getId());

        Task lettuce = service.createSubtask(salad.getId(), "lettuce");
        Task dressing = service.createSubtaskAfter(salad.getId(), "dressing", lettuce.getId());
        Task chicken = service.createSubtaskAfter(salad.getId(), "chicken", dressing.getId());

        // oh, we need cheese too
        Task cheese = service.createSubtask(tacos.getId(), "cheese");

        Task ham = service.createSubtask(lunch.getId(), "deli ham");
        Task cheese2 = service.createSubtaskAfter(lunch.getId(), "cheese", ham.getId());
        Task bread = service.createSubtaskAfter(lunch.getId(), "bread", cheese2.getId());

        muppetView("Ingredients");

        Task costco = service.createSubtask(groceries.getId(), "Costco");
        Task winco = service.createSubtaskAfter(groceries.getId(), "Winco", costco.getId());

        service.resetSubtasks(winco.getId(), new long[] {
                meat.getId(),
        });
        service.resetSubtasks(winco.getId(), new long[] {
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
        });
        service.resetSubtasks(winco.getId(), new long[] {
                dressing.getId(),
                lettuce.getId(),
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
        });
        service.resetSubtasks(winco.getId(), new long[] {
                dressing.getId(),
                lettuce.getId(),
                chicken.getId(),
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
        });
        service.resetSubtasks(winco.getId(), new long[] {
                dressing.getId(),
                lettuce.getId(),
                ham.getId(),
                chicken.getId(),
                meat.getId(),
                tortillas.getId(),
                salsa.getId(),
                bread.getId(),
        });
        service.resetSubtasks(costco.getId(), new long[] {
                cheese.getId(),
        });
        service.renameTask(cheese.getId(), "cheese (2)");
        service.deleteTask(cheese2.getId());

        muppetView("Shopping");
    }

    private void muppetView(String header) {
        taskRepo.flush();
        entityManager.clear();
        System.out.println(renderTree(header, service.getTaskLists(alice)));
    }

}
