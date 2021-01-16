package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@DiscriminatorValue("Recipe")
@JsonTypeName("Recipe")
public class Recipe extends Ingredient implements AggregateIngredient, Owned {

    // this will gracefully store the same way as an @Embedded Acl will
    @ManyToOne
    private User owner;

    // these will gracefully emulate AccessControlled's owner property
    @JsonIgnore // but hide it from the client :)
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    // end access control emulation

    @Getter
    @Setter
    private String externalUrl;

    @Getter
    @Setter
    private String directions;

    @Getter
    @Setter
    private Integer yield;

    @Getter
    @Setter
    private Integer calories;

    @Getter
    @Setter
    private String photo; //maybe use embeddable to grow more information about reference?
    public boolean hasPhoto() {
        return photo != null;
    }

    /**
     * Time is stored in milliseconds
     */
    @Getter
    @Setter
    private Integer totalTime;

    @ElementCollection
    @OrderBy("_idx, raw")
    private List<IngredientRef> ingredients;

    @JsonFormat(pattern = "yyyy-mm-dd")
    @Getter
    @Setter
    private Date created_at;

    @JsonFormat(pattern = "yyyy-mm-dd")
    @Getter
    @Setter
    private Date updated_at;

    public Recipe() {
    }

    public Recipe(String name) {
        setName(name);
    }

    public List<IngredientRef> getIngredients() {
        return this.ingredients;
    }

    public void setIngredients(List<IngredientRef> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public void addIngredient(Quantity quantity, Ingredient ingredient, String preparation) {
        ensureIngredients();
        ingredients.add(new IngredientRef(quantity, ingredient, preparation));
    }

    private void ensureIngredients() {
        if (ingredients == null) ingredients = new LinkedList<>();
    }

    public void addRawIngredient(String raw) {
        ensureIngredients();
        ingredients.add(new IngredientRef(raw));
    }

    @PrePersist
    protected void onCreate() {
        this.created_at = new Date();
        ensureRefOrder();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated_at = new Date();
        ensureRefOrder();
    }

    private void ensureRefOrder() {
        if (ingredients == null) return;
        int order = 0;
        for (IngredientRef ref : ingredients)
            ref.set_idx(order++);
    }

    @Override
    @JsonIgnore
    public Collection<IngredientRef> assemblePantryItemRefs() {
        LinkedList<IngredientRef> refs = new LinkedList<>();
        if (ingredients == null) return refs;
        for (IngredientRef ref : ingredients) {
            if (! ref.hasIngredient()) continue;
            Ingredient ingredient = ref.getIngredient();
            if (ingredient instanceof PantryItem) {
                refs.add( ref);
            } else if (ingredient instanceof AggregateIngredient) {
                refs.addAll(((AggregateIngredient) ingredient).assemblePantryItemRefs());
            } else {
                throw new IllegalStateException("Recipe #" + getId() + " has non-" + PantryItem.class.getSimpleName() + ", non-" + AggregateIngredient.class.getSimpleName() + " IngredientRef<" + (ingredient == null ? "null" : ingredient.getClass().getSimpleName()) + ">?!");
            }
        }
        return refs;
    }

    @Override
    @JsonIgnore
    public Collection<IngredientRef> assembleRawIngredientRefs() {
        LinkedList<IngredientRef> refs = new LinkedList<>();
        if (ingredients == null) return refs;
        for (IngredientRef ref : ingredients) {
            if (ref.hasIngredient()) {
                Ingredient ingredient = ref.getIngredient();
                if (ingredient instanceof AggregateIngredient) {
                    refs.addAll(((AggregateIngredient) ingredient).assembleRawIngredientRefs());
                }
            } else {
                refs.add(ref);
            }
        }
        return refs;
    }

    @Override
    public String toString() {
        return getName();
    }

}
