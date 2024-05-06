package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Setter
@Getter
@Entity
@Table(name = "pantry_item_duplicates")
@Immutable
public class PantryItemDuplicate {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    private PantryItem pantryItem;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    private PantryItem duplicate;
    private boolean loose;
    private float matchRank;

}
