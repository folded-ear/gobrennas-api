package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Setter
@Entity
@DiscriminatorValue("Recipe")
@JsonTypeName("Recipe")
@Audited
public class Recipe extends Ingredient implements AggregateIngredient, Owned {

    // this will gracefully store the same way as an @Embedded Acl will
    @ManyToOne(fetch = FetchType.LAZY)
    @NotAudited
    private User owner;

    // these will gracefully emulate AccessControlled's owner property
    @JsonIgnore // but hide it from the client :)
    public User getOwner() { return owner; }

    // end access control emulation

    @Getter
    @NotAudited
    private String externalUrl;

    @Getter
    @NotAudited
    private String directions;

    @Getter
    @NotAudited
    private Integer yield;

    @Getter
    @NotAudited
    private Integer calories;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "file.objectKey", column = @Column(name = "photo")),
            @AttributeOverride(name = "file.contentType", column = @Column(name = "photo_type")),
            @AttributeOverride(name = "file.size", column = @Column(name = "photo_size")),
            @AttributeOverride(name = "focusTop", column = @Column(name = "photo_focus_top")),
            @AttributeOverride(name = "focusLeft", column = @Column(name = "photo_focus_left"))
    })
    @NotAudited
    private Photo photo;
    public Photo getPhoto() {
        return getPhoto(false);
    }
    public Photo getPhoto(boolean create) {
        if (create && this.photo == null) {
            this.photo = new Photo();
        }
        return this.photo;
    }

    public void setPhoto(S3File file) {
        getPhoto(true).setFile(file);
    }
    public void clearPhoto() {
        if (hasPhoto()) {
            photo.clearFile();
        }
    }
    public boolean hasPhoto() {
        return photo != null && photo.hasFile();
    }

    /**
     * Time is stored in milliseconds
     */
    @Getter
    @NotAudited
    private Integer totalTime;

    @ElementCollection
    @OrderBy("_idx, raw")
    @NotAudited
    private List<IngredientRef> ingredients;

    public Recipe() {
    }

    public Recipe(String name) {
        setName(name);
    }

    public List<IngredientRef> getIngredients() {
        return this.ingredients;
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
    protected void onPrePersist() {
        super.onPrePersist();
        ensureRefOrder();
    }

    @PreUpdate
    protected void onPreUpdate() {
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
