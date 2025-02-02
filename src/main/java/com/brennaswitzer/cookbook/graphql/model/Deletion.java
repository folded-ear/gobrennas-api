package com.brennaswitzer.cookbook.graphql.model;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Named;
import lombok.Value;

@Value
public class Deletion {

    Long id;
    String name;

    public static <T extends Identified> Deletion of(T it) {
        String name = it instanceof Named named
                ? named.getName()
                : it.getClass().getSimpleName();
        return new Deletion(it.getId(), name);
    }

}
