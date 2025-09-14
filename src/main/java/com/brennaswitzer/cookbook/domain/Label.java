package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.repositories.event.IngredientLabelFulltextListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.Objects;

@Setter
@Getter
@Entity
@EntityListeners(IngredientLabelFulltextListener.class)
@BatchSize(size = 50)
public class Label implements Identified {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    private Long id;

    @NotNull
    @Column(unique = true)
    private String name;

    public Label() {}

    public Label(String name) {
        setName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label l)) return false;
        return Objects.equals(name, l.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
