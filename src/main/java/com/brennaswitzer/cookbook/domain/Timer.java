package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.time.Duration;
import java.time.Instant;

/**
 * I represent a user-controlled timer with second granularity. A timer can be
 * in three states: running, paused, and complete. It starts out running, and
 * may be paused and then resumed any number of times. Extra time may be added
 * until complete as well. Once its duration plus extra time has elapsed, it
 * becomes complete and may no longer be paused or have time added.
 */
@Entity
public class Timer extends BaseEntity implements AccessControlled {

    @Embedded
    @NotNull
    @Getter
    @Setter
    private Acl acl = new Acl();

    @Column(updatable = false)
    @Getter
    private int duration;

    @Getter
    private int extraTime;

    @Getter
    @Setter
    private Instant pausedAt;

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Timer durations must be positive, but " + duration + " isn't.");
        }
        if (this.duration > 0) {
            throw new UnsupportedOperationException("Timer durations cannot be changed; do you want to add extra time?");
        }
        this.duration = duration;
    }

    public void addExtraTime(int extraTime) {
        addExtraTime(extraTime, Instant.now());
    }

    public void addExtraTime(int extraTime, Instant asOf) {
        if (isComplete(asOf)) {
            throw new IllegalStateException("Extra time cannot be added after a timer is complete");
        }
        this.extraTime += extraTime;
    }

    public boolean isPaused() {
        return isPaused(Instant.now());
    }

    public boolean isPaused(Instant asOf) {
        return pausedAt != null && pausedAt.isBefore(asOf);
    }

    public boolean isComplete() {
        return isComplete(Instant.now());
    }

    public boolean isComplete(Instant asOf) {
        return getRemaining(asOf) <= 0;
    }

    public boolean isRunning() {
        return isRunning(Instant.now());
    }

    public boolean isRunning(Instant asOf) {
        return !isPaused() && getRemaining(asOf) > 0;
    }

    /**
     * I return the amount of time remaining on the timer, which may be negative
     * if it has already completed.
     */
    public int getRemaining() {
        return getRemaining(Instant.now());
    }

    public int getRemaining(Instant asOf) {
        var end = getEndAt(asOf);
        return between(asOf, end);
    }

    private int between(Instant start, Instant end) {
        val dur = Duration.between(start, end);
        val sec = (int) dur.getSeconds();
        return dur.getNano() < 500_000_000 ? sec : (sec + 1);
    }

    /**
     * I return when the timer will end, or has ended if it has already
     * completed. If the timer is currently paused, return the end time <em>as
     * if it were immediately resumed</em>.
     */
    public Instant getEndAt() {
        return getEndAt(Instant.now());
    }

    public Instant getEndAt(Instant asOf) {
        var base = getCreatedAt()
                .plusSeconds(duration)
                .plusSeconds(extraTime);
//        base = base.minusNanos(base.getNano());
        if (isPaused(asOf))
            base = base.plusSeconds(between(pausedAt, asOf));
        return base;
    }

    public void pause() {
        pause(Instant.now());
    }

    public void pause(Instant asOf) {
        if (pausedAt != null) {
            throw new IllegalStateException("Timer is already paused");
        }
        if (isComplete(asOf)) {
            throw new IllegalStateException("Timer is already complete");
        }
        pausedAt = asOf;
    }

    public void resume() {
        resume(Instant.now());
    }

    public void resume(Instant asOf) {
        if (pausedAt == null) {
            throw new IllegalStateException("Timer is not paused");
        }
        if (asOf.isBefore(pausedAt)) {
            throw new IllegalArgumentException("Resume at " + asOf + " makes no sense; not paused until " + pausedAt);
        }
        extraTime += between(pausedAt, asOf);
        pausedAt = null;
    }

}
