package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.ValueUtils;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("plan")
public class Plan extends PlanItem implements AccessControlled {

    // generated at http://medialab.github.io/iwanthue/
    private static final String[] COLORS = {
            "#cb4771",
            "#caa29e",
            "#783b32",
            "#d14f32",
            "#c9954c",
            "#cbd152",
            "#56713c",
            "#6dce55",
            "#8dd4aa",
            "#77adc2",
            "#6a7dc8",
            "#3b3a41",
            "#7145ca",
            "#552b6b",
            "#c583bd",
            "#cc4ac0" };

    @Embedded
    @NotNull
    @Getter
    @Setter
    private Acl acl = new Acl();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private Set<PlanBucket> buckets;

    @OneToMany(mappedBy = "trashBin", cascade = CascadeType.ALL)
    @BatchSize(size = 50)
    private Set<PlanItem> trashBinItems;

    private String color;

    public Plan() {
    }

    public Plan(String name) {
        super(name);
    }

    public Plan(User owner, String name) {
        super(name);
        setOwner(owner);
    }

    @Override
    public void setParent(PlanItem parent) {
        throw new UnsupportedOperationException("Plans can't have parents");
    }

    @Override
    public Plan getPlan() {
        return this;
    }

    public Set<PlanBucket> getBuckets() {
        if (buckets == null) {
            buckets = new HashSet<>();
        }
        return buckets;
    }

    public int getBucketCount() {
        return buckets == null ? 0 : buckets.size();
    }

    public boolean hasBuckets() {
        return ValueUtils.hasValue(buckets);
    }

    public Set<PlanItem> getTrashBinItems() {
        if (trashBinItems == null) {
            trashBinItems = new HashSet<>();
        }
        return trashBinItems;
    }

    public boolean hasTrash() {
        return ValueUtils.hasValue(trashBinItems);
    }

    public User getOwner() {
        return getAcl().getOwner();
    }

    public String getColor() {
        if (color == null) {
            color = COLORS[(int) (get_eqkey() % COLORS.length)];
        }
        return color;
    }

    public void setColor(String color) {
        this.color = StringUtils.hasText(color) ? color : null;
    }

}
