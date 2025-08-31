package com.brennaswitzer.cookbook.graphql.model;

import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Recipe;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Set;

public class Section {

    private final Recipe delegate;

    public Section(Recipe delegate) {
        this.delegate = delegate;
    }

    public static boolean isSection(IngredientRef ref) {
        return ref.isSection()
               && ref.hasIngredient()
               && Hibernate.unproxy(ref.getIngredient()) instanceof Recipe;
    }

    public static Section from(IngredientRef ref) {
        if (!isSection(ref)) {
            throw new IllegalArgumentException("Ref is not a section: " + ref);
        }
        return new Section((Recipe) Hibernate.unproxy(ref.getIngredient()));
    }

    public Recipe getSectionOf() {
        return delegate.getSectionOf();
    }

    public String getDirections() {
        return delegate.getDirections();
    }

    public List<IngredientRef> getIngredients() {
        return delegate.getIngredients();
    }

    public String getName() {
        return delegate.getName();
    }

    public Set<Label> getLabels() {
        return delegate.getLabels();
    }

    public Long getId() {
        return delegate.getId();
    }

}
