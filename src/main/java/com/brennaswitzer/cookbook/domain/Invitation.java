package com.brennaswitzer.cookbook.domain;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Entity
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Invitation extends BaseEntity implements Owned {

    @Embeddable
    @Getter
    @Setter
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Grant {

        @Embedded
        @Nonnull
        EntityRef entityRef;

        @Nonnull
        AccessLevel level;

    }

    /**
     * The user who created (and presumably sent) the invitation.
     */
    @ManyToOne
    @Nonnull
    User owner;

    /**
     * The secret code allowing a visitor to access to the invitation.
     */
    @Nonnull
    String secretCode;

    /**
     * The status of the invitation.
     */
    @Nonnull
    @Builder.Default
    InvitationStatus status = InvitationStatus.SENT;

    /**
     * When the invitation automatically becomes {@link InvitationStatus#EXPIRED}
     * if still {@link InvitationStatus#SENT}. Defaults to a week from midnight.
     */
    @Nonnull
    @Builder.Default
    Instant expiresAt = ZonedDateTime.now()
            .truncatedTo(ChronoUnit.DAYS)
            .plusDays(8)
            .toInstant();

    /**
     * Optional grant to issue a user who accepts the invitation, in addition to
     * establishing friendship.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "entityRef.className", column = @Column(name = "grant_entity_type")),
            @AttributeOverride(name = "entityRef.id", column = @Column(name = "grant_entity_id")),
            @AttributeOverride(name = "level", column = @Column(name = "grant_level")),
    })
    @Nullable
    Grant grant;

    /**
     * The user who received this invitation, perhaps via cloning.
     */
    @ManyToOne
    @Nullable
    User recipient;

    /**
     * The {@link Friendship}s which came from this invitation. Always either
     * empty or size two.
     */
    @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
    Collection<Friendship> friendships;

    /**
     * How many times this invitation may be cloned, while still
     * {@link InvitationStatus#SENT}. Defaults to zero.
     */
    @Builder.Default
    int cloneLimit = 0;

    /**
     * How many times this invitation has been cloned. Only {@link #cloneLimit}
     * clones are permitted, and only while still {@link InvitationStatus#SENT}.
     */
    @Builder.Default
    int cloneCount = 0;

}
