package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlanServiceTreeMutationTest {

    private TaskRepository repo;

    private PlanService service;

    @BeforeEach
    public void _setUpRepo() {
        final Map<Long, Task> database = new HashMap<>();
        final AtomicLong idSeq = new AtomicLong();
        repo = Mockito.mock(TaskRepository.class);
        Mockito.doAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getId() != null) return t;
            t.setId(idSeq.incrementAndGet());
            database.put(t.getId(), t);
            if (t.hasSubtasks()) {
                t.getOrderedSubtasksView().forEach(repo::save);
            }
            return t;
        }).when(repo).save(Mockito.any());
        Mockito.doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return database.get(id);
        }).when(repo).getOne(Mockito.anyLong());
        service = new PlanService() {
            @Override
            protected Task getTaskById(Long id, AccessLevel requiredAccess) {
                // Just skip the access checks. This is a smell that says the
                // service is doing multiple things!
                return taskRepo.getOne(id);
            }
        };
        service.taskRepo = repo;
    }

    private void checkKids(Task parent, Task... kids) {
        assertEquals(
                Arrays.asList(kids),
                parent.getOrderedSubtasksView(),
                "Children of " + parent + " are wrong:");
    }

    @Test
    public void moveFirstToTop() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(l,null, a);

        checkKids(l, a, b, c);
    }

    @Test
    public void moveMiddleToTop() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(l, null, b);

        checkKids(l, b, a, c);
    }

    @Test
    public void moveMiddleToLast() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(l, c, b);

        checkKids(l, a, c, b);
    }

    @Test
    public void moveLastToTop() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(l, null, c);

        checkKids(l, c, a, b);
    }

    @Test
    public void moveUnderChildlessPeer() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(a, null, b);

        checkKids(l, a, c);
        checkKids(a, b);
    }

    @Test
    public void moveToFirstChildOfPeer() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                a1 = new Task("a1").of(a),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(a, null, b);

        checkKids(l, a, c);
        checkKids(a, b, a1);
    }

    @Test
    public void moveToLastChildOfPeer() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                a1 = new Task("a1").of(a),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(a, a1, b);

        checkKids(l, a, c);
        checkKids(a, a1, b);
    }

    private void mutate(Task parent, Task after, Task... items) {
        service.mutateTree(
                Arrays.stream(items).map(Task::getId).collect(Collectors.toList()),
                parent.getId(),
                after == null ? null : after.getId());
    }

    @Test
    public void moveSubtreeAfterPeer() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                a1 = new Task("a1").of(a),
                b = new Task("b").of(l),
                c = new Task("c").of(l);
        repo.save(l);

        mutate(l, b, a);

        checkKids(l, b, a, c);
        checkKids(a, a1);
    }

    @Test
    public void crossTreeUpward() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                b1 = new Task("b1").of(b),
                b2 = new Task("b2").of(b),
                b3 = new Task("b3").of(b),
                c = new Task("c").of(l),
                d = new Task("d").of(l),
                d1 = new Task("d1").of(d),
                d2 = new Task("d2").of(d),
                d3 = new Task("d3").of(d),
                e = new Task("e").of(l);
        repo.save(l);

        mutate(b, b2, d2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b1, b2, d2, b3);
        checkKids(d, d1, d3);
    }

    @Test
    public void crossTreeDownward() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                b1 = new Task("b1").of(b),
                b2 = new Task("b2").of(b),
                b3 = new Task("b3").of(b),
                c = new Task("c").of(l),
                d = new Task("d").of(l),
                d1 = new Task("d1").of(d),
                d2 = new Task("d2").of(d),
                d3 = new Task("d3").of(d),
                e = new Task("e").of(l);
        repo.save(l);

        mutate(d, d2, b2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b1, b3);
        checkKids(d, d1, d2, b2, d3);
    }

    @Test
    public void nestBlock() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                c = new Task("c").of(l),
                d = new Task("d").of(l);
        repo.save(l);

        mutate(a, null, b, c);

        checkKids(l, a, d);
        checkKids(a, b, c);
    }

    @Test
    public void unnestBlock() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                a1 = new Task("a1").of(a),
                a2 = new Task("a2").of(a),
                b = new Task("b").of(l);
        repo.save(l);

        mutate(l, a, a1, a2);

        checkKids(l, a, a1, a2, b);
        checkKids(a);
    }

    @Test
    public void dragBlockAfter() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                b1 = new Task("b1").of(b),
                b2 = new Task("b2").of(b),
                b3 = new Task("b3").of(b),
                c = new Task("c").of(l),
                d = new Task("d").of(l),
                d1 = new Task("d1").of(d),
                d2 = new Task("d2").of(d),
                d3 = new Task("d3").of(d),
                e = new Task("e").of(l);
        repo.save(l);

        mutate(d, d2, b1, b2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b3);
        checkKids(d, d1, d2, b1, b2, d3);
    }

    @Test
    public void dragBlockUnder() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                b = new Task("b").of(l),
                b1 = new Task("b1").of(b),
                b2 = new Task("b2").of(b),
                b3 = new Task("b3").of(b),
                c = new Task("c").of(l),
                d = new Task("d").of(l),
                d1 = new Task("d1").of(d),
                d2 = new Task("d2").of(d),
                d3 = new Task("d3").of(d),
                e = new Task("e").of(l);
        repo.save(l);

        mutate(d2, null, b1, b2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b3);
        checkKids(d, d1, d2, d3);
        checkKids(d2, b1, b2);
    }

    @Test
    public void refuseToCreateCycles() {
        Task l = new TaskList("the list"),
                a = new Task("a").of(l),
                a1 = new Task("a1").of(a);
        repo.save(l);

        assertThrows(IllegalArgumentException.class, () ->
                mutate(a1, null, a));
    }

}
