package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.repositories.event.RecipeIngredientFulltextListener;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@EntityListeners(RecipeIngredientFulltextListener.class)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PantryItem.class, name = "PantryItem"),
        @JsonSubTypes.Type(value = Recipe.class, name = "Recipe")
})
public abstract class Ingredient extends BaseEntity implements Identified, Named, Labeled {

    public static Comparator<Ingredient> BY_NAME = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getName().compareToIgnoreCase(b.getName());
    };

    @Getter
    @Setter
    private String name;

    @ElementCollection
    private Set<LabelRef> labels = new HashSet<>();

    public Ingredient() {}

    Ingredient(String name) {
        setName(name);
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
