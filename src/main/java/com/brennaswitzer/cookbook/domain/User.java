package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User extends BaseEntity {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private AuthProvider provider;

    @Getter
    @Setter
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

    public boolean isOwnerOf(Owned owned) {
        return equals(owned.getOwner());
    }
}
