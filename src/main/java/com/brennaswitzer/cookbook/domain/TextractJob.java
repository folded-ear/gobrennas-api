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

    @Column(name = "status_id")
    @Setter(AccessLevel.NONE)
    private TextractJobStatus status = TextractJobStatus.PENDING;

    @Setter(AccessLevel.NONE)
    private String errorMessage;

    @Setter(AccessLevel.NONE)
    private boolean retryable;

    @ElementCollection
    @Setter(AccessLevel.NONE)
    private Set<Line> lines;

    public boolean isReady() {
        return status == TextractJobStatus.READY;
    }

    /**
     * I become ready, with the passed lines.
     */
    public void succeed(Set<Line> lines) {
        this.status = TextractJobStatus.READY;
        this.lines = lines;
        this.errorMessage = null;
        this.retryable = false;
    }

    /**
     * I become an error, for the passed reason.
     */
    public void fail(String message, boolean retryable) {
        this.status = TextractJobStatus.ERROR;
        this.errorMessage = message;
        this.retryable = retryable;
    }

    /**
     * I become pending again.
     */
    public void retry() {
        this.status = TextractJobStatus.PENDING;
        this.errorMessage = null;
        this.retryable = false;
    }

}
