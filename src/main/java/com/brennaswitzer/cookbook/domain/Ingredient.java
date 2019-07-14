package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;
import java.util.Comparator;

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
public abstract class Ingredient implements Identified {

    public static Comparator<Ingredient> BY_NAME = (a, b) ->
            a.getName().compareToIgnoreCase(b.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    Ingredient() {
    }

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

}

