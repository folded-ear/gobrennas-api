package com.brennaswitzer.cookbook.domain;

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
        return buckets != null && !buckets.isEmpty();
    }

    public Set<PlanItem> getTrashBinItems() {
        if (trashBinItems == null) {
            trashBinItems = new HashSet<>();
        }
        return trashBinItems;
    }

    public boolean hasTrash() {
        return trashBinItems != null && !trashBinItems.isEmpty();
    }

    public User getOwner() {
        return getAcl().getOwner();
    }

    public String getColor() {
        if (color == null) {
            // generated at http://medialab.github.io/iwanthue/
            color = switch ((int) (get_eqkey() % 16)) {
                case 0 -> "#cb4771";
                case 1 -> "#caa29e";
                case 2 -> "#783b32";
                case 3 -> "#d14f32";
                case 4 -> "#c9954c";
                case 5 -> "#cbd152";
                case 6 -> "#56713c";
                case 7 -> "#6dce55";
                case 8 -> "#8dd4aa";
                case 9 -> "#77adc2";
                case 10 -> "#6a7dc8";
                case 11 -> "#3b3a41";
                case 12 -> "#7145ca";
                case 13 -> "#552b6b";
                case 14 -> "#c583bd";
                default -> "#cc4ac0";
            };
        }
        return color;
    }

    public void setColor(String color) {
        this.color = StringUtils.hasText(color) ? color : null;
    }

}
