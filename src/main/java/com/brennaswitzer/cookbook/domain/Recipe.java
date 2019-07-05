package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@DiscriminatorValue("Recipe")
@JsonTypeName("PantryItem")
public class Recipe extends Ingredient implements AggregateIngredient {

    @NotBlank(message = "A title is required")
    private String title;

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

    public Recipe(String title) {
        setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getName() {
        // todo: this was an expedient way to deal with the name/title model
        //  issues that I'm deliberately _not_ fixing so that Brenna can, while
        //  still being able to use Ingredient.getName() in a polymorphic way
        //  in the interim. Much better would be to leave a @Deprecated getTitle
        //  in place which returns name (for backwards compatibility), and have
        //  all Ingredients use name. Period.
        return title;
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
