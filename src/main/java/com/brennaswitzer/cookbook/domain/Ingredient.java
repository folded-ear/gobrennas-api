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
public abstract class Ingredient {

    public static Comparator<Ingredient> BY_NAME = (a, b) ->
            a.getName().compareToIgnoreCase(b.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    Ingredient() {
    }

    Ingredient(String name) {
        setName(name);
    }

    public long getIngredientId() {
        return id;
    }

    public void setIngredientId(long ingredientId) {
        this.id = ingredientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

