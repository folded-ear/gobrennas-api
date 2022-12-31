package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;

/**
 * I represent a user-controlled timer with second granularity. A timer can be
 * in four states: running, paused, and complete. It starts out running, and may
 * be paused and then resumed any number of times. Once its duration has
 * elapsed, it becomes complete and may no longer be paused.
 */
public class Timer extends BaseEntity implements AccessControlled {

    @NotNull
    @Getter
    @Setter
    private Acl acl = new Acl();

    @Getter
    private int duration;

    @Getter
    @Setter
    private Instant pausedAt;

    @Getter
    @Setter
    private int pauseDuration;

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Timer durations must be positive, but " + duration + " isn't.");
        }
        this.duration = duration;
    }

    public boolean isPaused() {
        return pausedAt != null;
    }

    public boolean isComplete() {
        return isCompleteAt(Instant.now());
    }

    public boolean isCompleteAt(Instant now) {
        return getRemainingAt(now) <= 0;
    }

    public boolean isRunning() {
        return isRunningAt(Instant.now());
    }

    public boolean isRunningAt(Instant now) {
        return !isPaused() && getRemainingAt(now) > 0;
    }

    /**
     * I return the amount of time remaining on the timer, which may be negative
     * if it has already completed.
     */
    public int getRemaining() {
        return getRemainingAt(Instant.now());
    }

    public int getRemainingAt(Instant now) {
        val end = pausedAt == null ? now : pausedAt;
        val elapsed = Duration.between(getCreatedAt(), end);
        return duration - ((int) elapsed.getSeconds()) + pauseDuration;
    }

    public void pause() {
        pauseAt(Instant.now());
    }

    public void pauseAt(Instant now) {
        if (pausedAt != null) {
            throw new IllegalStateException("Timer is already paused");
        }
        if (isCompleteAt(now)) {
            throw new IllegalStateException("Timer is already complete");
        }
        pausedAt = now;
    }

    public void resume() {
        resumeAt(Instant.now());
    }

    public void resumeAt(Instant now) {
        if (pausedAt == null) {
            throw new IllegalStateException("Timer is not paused");
        }
        if (now.isBefore(pausedAt)) {
            throw new IllegalArgumentException("Resume at " + now + " makes no sense; not paused until " + pausedAt);
        }
        val sec = Duration.between(pausedAt, now).getSeconds();
        // sub-second pauses count as a full second, longer ones round down
        pauseDuration += sec == 0 ? 1 : sec;
        pausedAt = null;
    }

}
