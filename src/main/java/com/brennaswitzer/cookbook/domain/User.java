package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
}
