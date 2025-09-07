package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.repositories.event.IngredientFulltextListener;
import com.brennaswitzer.cookbook.repositories.event.RecipeIngredientFulltextListener;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@EntityListeners({
        IngredientFulltextListener.class,
        RecipeIngredientFulltextListener.class })
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PantryItem.class, name = "PantryItem"),
        @JsonSubTypes.Type(value = Recipe.class, name = "Recipe")
})
public abstract class Ingredient extends BaseEntity implements Named, Labeled {

    public static Comparator<Ingredient> BY_NAME = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getName().compareToIgnoreCase(b.getName());
    };

    @Getter
    private String name;

    @ManyToMany
    @JoinTable(inverseJoinColumns = { @JoinColumn(name = "label_id") })
    @BatchSize(size = 50)
    @Setter
    private Set<Label> labels;

    public Ingredient() {}

    Ingredient(String name) {
        setName(name);
    }

    public void setName(String name) {
        Assert.hasText(name, "Null/empty isn't a valid name");
        this.name = name.trim();
    }

    public Set<Label> getLabels() {
        if (labels == null) labels = new HashSet<>();
        return labels;
    }

    public void addLabel(Label label) {
        getLabels().add(label);
    }

    public void removeLabel(Label label) {
        if (labels == null) return;
        labels.remove(label);
    }

    public void clearLabels() {
        if (labels == null) return;
        labels.clear();
    }

}
