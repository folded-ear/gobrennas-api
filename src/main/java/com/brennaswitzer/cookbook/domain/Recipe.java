package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

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

    private String externalUrl;

    private String directions;

    @ElementCollection
    @OrderBy("_idx, raw")
    private List<IngredientRef> ingredients;

    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date created_at;

    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date updated_at;

    public Recipe() {
    }

    public Recipe(String name) {
        setName(name);
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
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
        ingredients.add(new IngredientRef<>(quantity, ingredient, preparation));
    }

    private void ensureIngredients() {
        if (ingredients == null) ingredients = new LinkedList<>();
    }

    public void addRawIngredient(String raw) {
        ensureIngredients();
        ingredients.add(new IngredientRef<>(raw));
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
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
    public Collection<IngredientRef<PantryItem>> assemblePantryItemRefs() {
        LinkedList<IngredientRef<PantryItem>> refs = new LinkedList<>();
        if (ingredients == null) return refs;
        for (IngredientRef ref : ingredients) {
            if (! ref.hasIngredient()) continue;
            Ingredient ingredient = ref.getIngredient();
            if (ingredient instanceof PantryItem) {
                //noinspection unchecked
                refs.add((IngredientRef<PantryItem>) ref);
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
