package com.brennaswitzer.cookbook.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String external_url;
    private String ingredients;
    private String directions;

    private Date created_at;
    private Date updated_at;

    @PrePersist
    protected void onCreate() {
        this.created_at = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated_at = new Date();
    }

}
