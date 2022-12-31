package com.brennaswitzer.cookbook.domain;

import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
        val t = new Timer();
        t.setCreatedAt(NOW);
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
        Thread.sleep(550);
        assertTrue(t.isRunning());
        assertFalse(t.isPaused());
        assertFalse(t.isComplete());
        assertEquals(1, t.getRemaining());
        t.pause();
        assertFalse(t.isRunning());
        assertTrue(t.isPaused());
        assertFalse(t.isComplete());
        assertEquals(1, t.getRemaining());
        Thread.sleep(550);
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
    void getRemaining_neverPaused() {
        val t = setFor(15);
        assertEquals(7, t.getRemainingAt(in(8)));
        assertEquals(5, t.getRemainingAt(in(10)));
        assertEquals(0, t.getRemainingAt(in(15)));
        assertEquals(-2, t.getRemainingAt(in(17)));
    }

    @Test
    void getRemaining_previouslyPaused() {
        val t = setFor(15);
        t.setPauseDuration(6);
        assertEquals(6, t.getRemainingAt(in(15)));
        assertEquals(0, t.getRemainingAt(in(21)));
        assertEquals(-3, t.getRemainingAt(in(24)));
    }

    @Test
    void getRemaining_currentlyPaused() {
        val t = setFor(15);
        t.pauseAt(in(5));
        assertEquals(10, t.getRemainingAt(in(5)));
        assertEquals(10, t.getRemainingAt(in(6)));
        assertEquals(10, t.getRemainingAt(in(99999)));
    }

    @Test
    void getRemaining_previouslyAndCurrentlyPaused() {
        val t = setFor(15);
        t.setPauseDuration(6);
        t.setPausedAt(in(10));
        assertEquals(11, t.getRemainingAt(in(10)));
        assertEquals(11, t.getRemainingAt(in(50)));
        t.resumeAt(in(30));
        assertEquals(11, t.getRemainingAt(in(30)));
        assertEquals(6, t.getRemainingAt(in(35)));
        assertEquals(-9, t.getRemainingAt(in(50)));
    }

    @Test
    void isCompleteOrRunning() {
        val t = setFor(10);
        assertFalse(t.isCompleteAt(in(5)));
        assertTrue(t.isRunningAt(in(5)));
        assertTrue(t.isCompleteAt(in(10)));
        assertFalse(t.isRunningAt(in(10)));
        assertTrue(t.isCompleteAt(in(15)));
        assertFalse(t.isRunningAt(in(15)));
    }

    @Test
    void isPausedOrRunning() {
        val t = setFor(10);
        assertFalse(t.isPaused());
        assertTrue(t.isRunningAt(in(2)));
        t.pauseAt(in(5));
        assertTrue(t.isPaused());
        assertFalse(t.isRunningAt(in(6)));
        t.resumeAt(in(7));
        assertFalse(t.isPaused());
        assertTrue(t.isRunningAt(in(9)));
        assertTrue(t.isRunningAt(in(11)));
        // complete at 12
        assertFalse(t.isPaused());
        assertFalse(t.isRunningAt(in(13)));
    }

    @Test
    void cantPauseCompleted() {
        assertThrows(IllegalStateException.class, () ->
                setFor(10).pauseAt(in(15)));
    }

    @Test
    void cantPausePaused() {
        val t = setFor(10);
        t.pauseAt(in(5));
        assertThrows(IllegalStateException.class, () ->
                t.pauseAt(in(7)));
    }

    @Test
    void cantResumeRunning() {
        val t = setFor(10);
        assertThrows(IllegalStateException.class, () ->
                t.resumeAt(in(1)));
        t.pauseAt(in(3));
        t.resumeAt(in(5));
        assertThrows(IllegalStateException.class, () ->
                t.resumeAt(in(7)));
    }

    @Test
    void cantResumeBeforePause() {
        val t = setFor(10);
        t.pauseAt(in(3));
        assertThrows(IllegalArgumentException.class, () ->
                t.resumeAt(in(1)));
    }

    @Test
    void cantSetNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () ->
                setFor(-1));
    }

}
