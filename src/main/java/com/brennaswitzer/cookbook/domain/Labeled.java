package com.brennaswitzer.cookbook.domain;

import java.util.Collection;
import java.util.Set;

public interface Labeled {
    Set<Label> getLabels();
    void addLabel(Label label);
    void removeLabel(Label label);

    default boolean hasLabel(Label label) {
        return getLabels().contains(label);
    }

    default void addLabels(Collection<Label> labels) {
        for (Label l : labels) {
            addLabel(l);
        }
    }

    default void clearLabels() {
        for (Label l : getLabels()) {
            removeLabel(l);
        }
    };
}
