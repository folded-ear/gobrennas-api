package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class LabelRef {

    @ManyToOne(fetch = FetchType.LAZY)
    private Label label;

    public LabelRef() {
    }

    public LabelRef(Label name) {
        setLabel(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelRef labelRef = (LabelRef) o;
        return label.equals(labelRef.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
