package com.brennaswitzer.cookbook.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Provenance {

    @Column(name = "prov_type")
    private Class<? extends Identified> type;

    @Column(name = "prov_id")
    private Long id;

    Provenance() {}

    public Provenance(Class<? extends Identified> type, Long id) {
        setType(type);
        setId(id);
    }

    public Provenance(Identified provenance) {
        this(provenance.getClass(), provenance.getId());
    }

    public Class<? extends Identified> getType() {
        return type;
    }

    public void setType(Class<? extends Identified> type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
