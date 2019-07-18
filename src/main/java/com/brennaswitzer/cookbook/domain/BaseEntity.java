package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.IdUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@SuppressWarnings("WeakerAccess")
@MappedSuperclass
public abstract class BaseEntity implements Identified {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(updatable = false)
    private final Long _eqkey = IdUtils.next(getClass());

    @NotNull
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Version
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public Long get_eqkey() {
        return this._eqkey;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onPersist() {
        Instant now = Instant.now();
        setCreatedAt(now);
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
        if (!getClass().isAssignableFrom(object.getClass())) return false;
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
