package com.brennaswitzer.cookbook.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
@MappedSuperclass
public abstract class BaseEntity implements Identified {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(updatable = false)
    private final Long _eqkey = make_eq_key();

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

    private static AtomicInteger _eq_seq = new AtomicInteger(0);
    private long make_eq_key() {
        long t = System.currentTimeMillis() / 1000;
        long c = getClass().getName().hashCode();
        int s = _eq_seq.getAndIncrement();
        t <<= 32; // 32 bits of epoch time
        c = (c & 0xFFFF) << 16; // 16 low-order bits of class name
        s &= 0xFFFF; // 16 low-order bits of sequence
//        System.out.printf("time   : %64s%n", Long.toBinaryString(t));
//        System.out.printf("class  : %64s%n", Long.toBinaryString(c));
//        System.out.printf("seq    : %64s%n", Long.toBinaryString(s));
//        System.out.printf("eq_key : %64s%n", Long.toBinaryString(t | c | s));
        return t | c | s;
    }

//    public static void main(String[] args) {
//        class Blerg extends BaseEntity {}
//        class Glerg extends BaseEntity {}
//        System.out.println(new Blerg().get_eqkey());
//        System.out.println(new Glerg().get_eqkey());
//        System.out.println(new Glerg().get_eqkey());
//    }

}
