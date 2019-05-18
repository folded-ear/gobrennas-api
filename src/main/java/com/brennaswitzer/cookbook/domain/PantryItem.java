package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@DiscriminatorValue("PantryItem")
public class PantryItem extends Ingredient {
    private String aisle;
}