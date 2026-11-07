package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * I am a read-only view of another user, exposing only what's appropriate to
 * show to someone other than that user. My id is that user's id.
 */
@Entity
@Table(name = "users")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend implements Identified {

    @Id
    private Long id;

    private String name;

    private String email;

    private String imageUrl;

}
