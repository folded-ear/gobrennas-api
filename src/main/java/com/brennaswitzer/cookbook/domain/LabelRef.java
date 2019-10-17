package com.brennaswitzer.cookbook.domain;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Embeddable
public class LabelRef {

    @ManyToOne
    private Label label;

    public LabelRef() {
    }

    public LabelRef(Label name) {
        setLabel(name);
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
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
