package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
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

    private PlanItemRepository repo;

    private PlanService service;

    @BeforeEach
    public void _setUpRepo() {
        final Map<Long, PlanItem> database = new HashMap<>();
        final AtomicLong idSeq = new AtomicLong();
        repo = Mockito.mock(PlanItemRepository.class);
        Mockito.doAnswer(invocation -> {
            PlanItem t = invocation.getArgument(0);
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
        }).when(repo).getReferenceById(Mockito.anyLong());
        service = new PlanService() {
            @Override
            protected PlanItem getTaskById(Long id, AccessLevel requiredAccess) {
                // Just skip the access checks. This is a smell that says the
                // service is doing multiple things!
                return taskRepo.getReferenceById(id);
            }
        };
        service.taskRepo = repo;
    }

    private void checkKids(PlanItem parent, PlanItem... kids) {
        assertEquals(
                Arrays.asList(kids),
                parent.getOrderedSubtasksView(),
                "Children of " + parent + " are wrong:");
    }

    @Test
    public void moveFirstToTop() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(l, null, a);

        checkKids(l, a, b, c);
    }

    @Test
    public void moveMiddleToTop() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(l, null, b);

        checkKids(l, b, a, c);
    }

    @Test
    public void moveMiddleToLast() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(l, c, b);

        checkKids(l, a, c, b);
    }

    @Test
    public void moveLastToTop() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(l, null, c);

        checkKids(l, c, a, b);
    }

    @Test
    public void moveUnderChildlessPeer() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(a, null, b);

        checkKids(l, a, c);
        checkKids(a, b);
    }

    @Test
    public void moveToFirstChildOfPeer() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                a1 = new PlanItem("a1").of(a),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(a, null, b);

        checkKids(l, a, c);
        checkKids(a, b, a1);
    }

    @Test
    public void moveToLastChildOfPeer() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                a1 = new PlanItem("a1").of(a),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(a, a1, b);

        checkKids(l, a, c);
        checkKids(a, a1, b);
    }

    private void mutate(PlanItem parent, PlanItem after, PlanItem... items) {
        service.mutateTree(
                Arrays.stream(items).map(PlanItem::getId).collect(Collectors.toList()),
                parent.getId(),
                after == null ? null : after.getId());
    }

    @Test
    public void moveSubtreeAfterPeer() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                a1 = new PlanItem("a1").of(a),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l);
        repo.save(l);

        mutate(l, b, a);

        checkKids(l, b, a, c);
        checkKids(a, a1);
    }

    @Test
    public void crossTreeUpward() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                b1 = new PlanItem("b1").of(b),
                b2 = new PlanItem("b2").of(b),
                b3 = new PlanItem("b3").of(b),
                c = new PlanItem("c").of(l),
                d = new PlanItem("d").of(l),
                d1 = new PlanItem("d1").of(d),
                d2 = new PlanItem("d2").of(d),
                d3 = new PlanItem("d3").of(d),
                e = new PlanItem("e").of(l);
        repo.save(l);

        mutate(b, b2, d2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b1, b2, d2, b3);
        checkKids(d, d1, d3);
    }

    @Test
    public void crossTreeDownward() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                b1 = new PlanItem("b1").of(b),
                b2 = new PlanItem("b2").of(b),
                b3 = new PlanItem("b3").of(b),
                c = new PlanItem("c").of(l),
                d = new PlanItem("d").of(l),
                d1 = new PlanItem("d1").of(d),
                d2 = new PlanItem("d2").of(d),
                d3 = new PlanItem("d3").of(d),
                e = new PlanItem("e").of(l);
        repo.save(l);

        mutate(d, d2, b2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b1, b3);
        checkKids(d, d1, d2, b2, d3);
    }

    @Test
    public void nestBlock() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                c = new PlanItem("c").of(l),
                d = new PlanItem("d").of(l);
        repo.save(l);

        mutate(a, null, b, c);

        checkKids(l, a, d);
        checkKids(a, b, c);
    }

    @Test
    public void unnestBlock() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                a1 = new PlanItem("a1").of(a),
                a2 = new PlanItem("a2").of(a),
                b = new PlanItem("b").of(l);
        repo.save(l);

        mutate(l, a, a1, a2);

        checkKids(l, a, a1, a2, b);
        checkKids(a);
    }

    @Test
    public void dragBlockAfter() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                b1 = new PlanItem("b1").of(b),
                b2 = new PlanItem("b2").of(b),
                b3 = new PlanItem("b3").of(b),
                c = new PlanItem("c").of(l),
                d = new PlanItem("d").of(l),
                d1 = new PlanItem("d1").of(d),
                d2 = new PlanItem("d2").of(d),
                d3 = new PlanItem("d3").of(d),
                e = new PlanItem("e").of(l);
        repo.save(l);

        mutate(d, d2, b1, b2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b3);
        checkKids(d, d1, d2, b1, b2, d3);
    }

    @Test
    public void dragBlockUnder() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                b = new PlanItem("b").of(l),
                b1 = new PlanItem("b1").of(b),
                b2 = new PlanItem("b2").of(b),
                b3 = new PlanItem("b3").of(b),
                c = new PlanItem("c").of(l),
                d = new PlanItem("d").of(l),
                d1 = new PlanItem("d1").of(d),
                d2 = new PlanItem("d2").of(d),
                d3 = new PlanItem("d3").of(d),
                e = new PlanItem("e").of(l);
        repo.save(l);

        mutate(d2, null, b1, b2);

        checkKids(l, a, b, c, d, e);
        checkKids(b, b3);
        checkKids(d, d1, d2, d3);
        checkKids(d2, b1, b2);
    }

    @Test
    public void refuseToCreateCycles() {
        PlanItem l = new Plan("the list"),
                a = new PlanItem("a").of(l),
                a1 = new PlanItem("a1").of(a);
        repo.save(l);

        assertThrows(IllegalArgumentException.class, () ->
                mutate(a1, null, a));
    }

}
