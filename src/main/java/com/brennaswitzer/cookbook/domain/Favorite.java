package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Favorite extends BaseEntity implements Owned {

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    private Long objectId;

    private String objectType;

}
