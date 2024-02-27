package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Favorite extends BaseEntity implements Owned {

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @Getter
    @Setter
    private Long objectId;

    @Getter
    @Setter
    private String objectType;

}
