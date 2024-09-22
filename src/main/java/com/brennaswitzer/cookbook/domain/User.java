package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User extends BaseEntity {

    private String name;

    private String email;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    public User() {
    }

    public User(String name, String username, String email, String password) {
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + email + ")";
    }

    public boolean owns(Owned owned) {
        return owned.isOwner(this);
    }
}
