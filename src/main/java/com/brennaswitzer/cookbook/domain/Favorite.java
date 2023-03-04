package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Favorite extends BaseEntity implements Owned {

    @Getter
    @Setter
    @ManyToOne
    private User owner;

    @Getter
    @Setter
    private Long objectId;

    @Getter
    @Setter
    private String objectType;

}
