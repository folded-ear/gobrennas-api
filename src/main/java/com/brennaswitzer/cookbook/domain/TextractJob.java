package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Setter
@Getter
@Entity
public class TextractJob extends BaseEntity implements Owned {

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Line {

        String text;

        // left is a SQL keyword, so to make querying easier, use x/y columns
        @SuppressWarnings("JpaDataSourceORMInspection")
        @Embedded
        @AttributeOverrides({
                @AttributeOverride(name = "left", column = @Column(name = "x")),
                @AttributeOverride(name = "top", column = @Column(name = "y")),
        })
        Box box;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Box {
        double left;
        double top;
        double width;
        double height;
    }

    @ManyToOne(optional = false)
    private User owner;

    @Embedded
    private S3File photo;
    public boolean hasPhoto() {
        return photo != null;
    }

    private boolean ready;

    @ElementCollection
    private Set<Line> lines;

}
