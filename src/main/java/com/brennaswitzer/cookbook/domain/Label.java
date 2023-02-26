package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.repositories.event.RecipeLabelFulltextListener;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@EntityListeners(RecipeLabelFulltextListener.class)
public class Label implements Identified {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Getter
    @Setter
    private Long id;

    @NotNull
    @Column(unique = true)
    @Getter
    @Setter
    private String name;

    public Label() {}

    public Label(String name) {
        setName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return Objects.equals(id, label.id) &&
                Objects.equals(name, label.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
