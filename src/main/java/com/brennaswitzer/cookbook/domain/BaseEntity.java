package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.IdUtils;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@MappedSuperclass
@SequenceGenerator( // can go anywhere - it's persistence context-wide
        name = "id_seq",
        sequenceName = "id_seq"
)
public abstract class BaseEntity implements Identified {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Getter
    @Setter
    private Long id;

    @NotNull
    @Column(updatable = false)
    @Getter
    private final Long _eqkey = IdUtils.next(getClass());

    @NotNull
    @Getter
    @Setter
    private Instant createdAt;

    @NotNull
    @Version
    @Getter
    private Instant updatedAt;

    @PrePersist
    protected void onPrePersist() {
        setCreatedAt(Instant.now());
    }

    /**
     * I force Hibernate to consider this entity dirty. If you want to use me,
     * you're probably incorrect, so think twice. I am useful for explicitly
     * flagging something dirty when one of its collections changes, which was
     * needed to switch the planner to poll-based 'changed since x' updates.
     */
    protected void markDirty() {
        updatedAt = Instant.now();
    }

    /**
     * I indicate object equality, which in this case means an assignable type
     * and the same {@link #get_eqkey()}. Using the {@code _eqkey} (which is
     * database-persisted) instead of the object's memory location allows for
     * proper operation across the persistence boundary. Using an assignable
     * type (instead of type equality) allows for proper operation across
     * persistence proxies. It has the side effect of allow subtypes to be
     * considered equal, but the {@code _eqkey} generator embeds type info which
     * will break such ties in the normal case.
     *
     * @param object The object to check for equality with this one
     * @return Whether the passed object is equal to this one
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (!BaseEntity.class.isAssignableFrom(object.getClass())) return false;
        return this.get_eqkey().equals(((BaseEntity) object).get_eqkey());
    }

    @Override
    public int hashCode() {
        return this.get_eqkey().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getId();
    }

}
