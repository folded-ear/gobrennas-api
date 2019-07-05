package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@DiscriminatorValue("Recipe")
@JsonTypeName("PantryItem")
public class Recipe extends Ingredient implements AggregateIngredient {

    private String displayTitle;

    private String external_url;

    private String directions;

    @Column(name = "raw_ingredients")
    private String rawIngredients;

    @ElementCollection
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

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getExternal_url() {
        return external_url;
    }

    public void setExternal_url(String external_url) {
        this.external_url = external_url;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

    public String getRawIngredients() {
        return rawIngredients;
    }

    public void setRawIngredients(String rawIngredients) {
        this.rawIngredients = rawIngredients;
    }

    public List<IngredientRef> getIngredients() {
        return this.ingredients;
    }

    public void setIngredients(List<IngredientRef> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        addIngredient(null, ingredient, null);
    }

    public void addIngredient(String quantity, Ingredient ingredient) {
        addIngredient(quantity, ingredient, null);
    }

    public void addIngredient(Ingredient ingredient, String preparation) {
        addIngredient(null, ingredient, preparation);
    }

    public void addIngredient(String quantity, Ingredient ingredient, String preparation) {
        if (ingredients == null) ingredients = new LinkedList<>();
        ingredients.add(new IngredientRef(quantity, ingredient, preparation));
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated_at = new Date();
    }

    @Override
    public Collection<IngredientRef> getPurchasableSchmankies() {
        LinkedList<IngredientRef> refs = new LinkedList<>();
        if (ingredients == null) return refs;
        for (IngredientRef ref : ingredients) {
            if (ref.getIngredient() instanceof AggregateIngredient) {
                refs.addAll(((AggregateIngredient) ref.getIngredient()).getPurchasableSchmankies());
            } else {
                refs.add(ref);
            }
        }
        return refs;
    }
}
