package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * I record a user acquiring an ingredient, at a time and place.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngredientAcquisition extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Ingredient ingredient;

    @NotNull
    private Instant acquiredAt;

    @NotNull
    @Embedded
    private Coordinates coordinates;

    public IngredientAcquisition(User user,
                                 Ingredient ingredient,
                                 Instant acquiredAt,
                                 Coordinates coordinates) {
        this.user = user;
        this.ingredient = ingredient;
        this.acquiredAt = acquiredAt;
        this.coordinates = coordinates;
    }

}
