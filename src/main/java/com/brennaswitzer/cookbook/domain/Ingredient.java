package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PantryItem.class, name = "PantryItem"),
        @JsonSubTypes.Type(value = Recipe.class, name = "Recipe")
})
public abstract class Ingredient implements Identified, Labeled {

    public static Comparator<Ingredient> BY_NAME = (a, b) ->
            a.getName().compareToIgnoreCase(b.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    private Set<LabelRef> labels = new HashSet<>();

    public Ingredient() {}

    Ingredient(String name) {
        setName(name);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Label> getLabels() {
        Set<Label> s = new HashSet<>();
        for (LabelRef ref : labels) {
            s.add(ref.getLabel());
        }
        return s;
    }

    public void addLabel(Label label) {
        labels.add(new LabelRef(label));
    }

    public void removeLabel(Label label) {
        labels.remove(new LabelRef(label));
    }

    public void clearLabels() {
        this.labels.clear();
    }


}

