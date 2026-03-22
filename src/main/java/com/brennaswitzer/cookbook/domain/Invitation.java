package com.brennaswitzer.cookbook.domain;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Entity
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Invitation extends BaseEntity {

    @Embeddable
    @Getter
    @Setter
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class Grant {

        @Embedded
        @Nonnull
        EntityRef entity;

        @Nonnull
        AccessLevel level;

    }

    /**
     * The user who created (and presumably sent) the invitation.
     */
    @ManyToOne
    @Nonnull
    User user;

    /**
     * The secret code allowing a visitor to access to the invitation.
     */
    @Nonnull
    String secretCode;

    /**
     * The status of the invitation.
     */
    @Nonnull
    InvitationStatus status = InvitationStatus.SENT;

    /**
     * When the invitation automatically becomes {@link InvitationStatus#WITHDRAWN}
     * if still {@link InvitationStatus#SENT}. Defaults to a week from midnight.
     */
    @Nonnull
    Instant expiresAt = ZonedDateTime.now()
            .truncatedTo(ChronoUnit.DAYS)
            .plusDays(8)
            .toInstant();

    /**
     * Optional grant to issue a user who accepts the invitation, as well as
     * establishing friendship.
     */
    @Embedded
    @Nullable
    Grant grant;

    /**
     * The {@link Friendship}s which came from this invitation. Always either
     * size two or empty.
     */
    @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
    Collection<Friendship> friendships;

    /**
     * How many times this invitation may be cloned, while still
     * {@link InvitationStatus#SENT}. Defaults to zero.
     */
    int cloneLimit = 0;

    /**
     * How many times this invitation has been cloned. Only {@link #cloneLimit}
     * clones are permitted, and only while still {@link InvitationStatus#SENT}.
     */
    int cloneCount = 0;

}
