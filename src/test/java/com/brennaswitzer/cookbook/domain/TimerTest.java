package com.brennaswitzer.cookbook.domain;

import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TimerTest {

    // Sat Dec 31 2022 12:00:00 PST
    private static final Instant NOW = Instant.ofEpochSecond(1672516800);

    private Instant in(int seconds) {
        return NOW.plus(seconds, ChronoUnit.SECONDS);
    }

    private Timer setFor(int seconds) {
        return setFor(seconds, NOW);
    }

    private Timer setFor(int seconds, Instant at) {
        val t = new Timer();
        t.setCreatedAt(at);
        t.setDuration(seconds);
        return t;
    }

    /*
     * This may be flaky, hence being @Disabled. There's a reason for all those
     * helper methods that accept an Instant, instead of relying on the clock
     * of the computer to tell time.
     */
    @Test
    @Disabled
    void doesItSmoke() throws InterruptedException {
        val t = new Timer();
        t.setCreatedAt(Instant.now());
        t.setDuration(1);
        assertEquals(1, t.getRemaining());
        Thread.sleep(200);
        assertTrue(t.isRunning());
        assertFalse(t.isPaused());
        assertFalse(t.isComplete());
        assertEquals(1, t.getRemaining());
        t.pause();
        assertFalse(t.isRunning());
        assertTrue(t.isPaused());
        assertFalse(t.isComplete());
        assertEquals(1, t.getRemaining());
        Thread.sleep(800);
        assertFalse(t.isRunning());
        assertTrue(t.isPaused());
        assertFalse(t.isComplete());
        assertEquals(1, t.getRemaining());
        t.resume();
        assertTrue(t.isRunning());
        assertFalse(t.isPaused());
        assertFalse(t.isComplete());
        assertEquals(1, t.getRemaining());
        Thread.sleep(1100);
        assertFalse(t.isRunning());
        assertFalse(t.isPaused());
        assertTrue(t.isComplete());
        assertEquals(0, t.getRemaining());
    }

    @Test
    void getEndAt() {
        val t = setFor(15);
        assertEquals(in(15), t.getEndAt());
        t.addExtraTime(5, in(10));
        assertEquals(in(20), t.getEndAt());
        t.pause(in(15));
        // paused w/ five seconds left, so always five seconds from now
        var now = in(123456);
        assertEquals(5, Duration.between(now, t.getEndAt(now)).getSeconds());
        t.resume(in(18));
        assertEquals(in(23), t.getEndAt());
    }

    @Test
    void getRemaining_neverPaused() {
        val t = setFor(15);
        assertEquals(7, t.getRemaining(in(8)));
        assertEquals(5, t.getRemaining(in(10)));
        assertEquals(0, t.getRemaining(in(15)));
        assertEquals(-2, t.getRemaining(in(17)));
    }

    @Test
    void getRemaining_currentlyPaused() {
        val t = setFor(15);
        t.pause(in(5));
        assertEquals(10, t.getRemaining(in(5)));
        assertEquals(10, t.getRemaining(in(6)));
        assertEquals(10, t.getRemaining(in(99999)));
    }

    @Test
    void getRemaining_previouslyAndCurrentlyPaused() {
        val t = setFor(15);
        t.addExtraTime(6, in(6));
        t.setPausedAt(in(10));
        assertEquals(11, t.getRemaining(in(10)));
        assertEquals(11, t.getRemaining(in(50)));
        t.resume(in(30));
        assertEquals(11, t.getRemaining(in(30)));
        assertEquals(6, t.getRemaining(in(35)));
        assertEquals(-9, t.getRemaining(in(50)));
    }

    @Test
    void isCompleteOrRunning() {
        val t = setFor(10);
        assertFalse(t.isComplete(in(5)));
        assertTrue(t.isRunning(in(5)));
        assertTrue(t.isComplete(in(10)));
        assertFalse(t.isRunning(in(10)));
        assertTrue(t.isComplete(in(15)));
        assertFalse(t.isRunning(in(15)));
    }

    @Test
    void isPausedOrRunning() {
        val t = setFor(10);
        assertFalse(t.isPaused());
        assertTrue(t.isRunning(in(2)));
        t.pause(in(5));
        assertTrue(t.isPaused());
        assertFalse(t.isRunning(in(6)));
        t.resume(in(7));
        assertFalse(t.isPaused());
        assertTrue(t.isRunning(in(9)));
        assertTrue(t.isRunning(in(11)));
        // complete at 12
        assertFalse(t.isPaused());
        assertFalse(t.isRunning(in(13)));
    }

    @Test
    void extraTime() {
        val t = setFor(10);
        assertEquals(10, t.getRemaining(NOW));
        assertEquals(0, t.getRemaining(in(10)));
        assertTrue(t.isComplete(in(12)));

        t.addExtraTime(5, in(5));

        assertEquals(15, t.getRemaining(NOW));
        assertEquals(5, t.getRemaining(in(10)));
        assertFalse(t.isComplete(in(12)));
        assertEquals(-1, t.getRemaining(in(16)));
    }

    @Test
    void cantPauseCompleted() {
        assertThrows(IllegalStateException.class, () ->
                setFor(10).pause(in(15)));
    }

    @Test
    void cantPausePaused() {
        val t = setFor(10);
        t.pause(in(5));
        assertThrows(IllegalStateException.class, () ->
                t.pause(in(7)));
    }

    @Test
    void cantResumeRunning() {
        val t = setFor(10);
        assertThrows(IllegalStateException.class, () ->
                t.resume(in(1)));
        t.pause(in(3));
        t.resume(in(5));
        assertThrows(IllegalStateException.class, () ->
                t.resume(in(7)));
    }

    @Test
    void cantResumeBeforePause() {
        val t = setFor(10);
        t.pause(in(3));
        assertThrows(IllegalArgumentException.class, () ->
                t.resume(in(1)));
    }

    @Test
    void cantSetNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                setFor(-1));
    }

    @Test
    void cantChangeDuration() {
        assertThrows(UnsupportedOperationException.class, () ->
                setFor(10).setDuration(15));
    }

    @Test
    void cantAddTimeToCompleted() {
        assertThrows(IllegalStateException.class, () ->
                setFor(1, in(-10)).addExtraTime(1));
    }

}
