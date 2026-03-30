package com.brennaswitzer.cookbook.domain;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends BaseEntity {

    /**
     * The user who owns this half of the friendship.
     */
    @ManyToOne
    @Nonnull
    User user;

    /**
     * The user who owns the other half of the friendship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Nonnull
    User friend;

    /**
     * The {@link Invitation} which facilitated this friendship.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Nonnull
    Invitation invitation;

}
