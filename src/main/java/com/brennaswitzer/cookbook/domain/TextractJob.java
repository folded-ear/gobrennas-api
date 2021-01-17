package com.brennaswitzer.cookbook.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Set;

@Entity
public class TextractJob extends BaseEntity {

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Embeddable
    static class Line {

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
    static class Box {
        double left;
        double top;
        double width;
        double height;
    }

    @Getter
    @Setter
    @ManyToOne
    private User owner;

    @Getter
    @Setter
    @Embedded
    private S3File photo;
    public boolean hasPhoto() {
        return photo != null;
    }

    @Getter
    @Setter
    private boolean ready;

    @Getter
    @Setter
    @ElementCollection
    private Set<Line> lines;

}
