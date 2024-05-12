package com.brennaswitzer.cookbook.graphql.model;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Named;
import lombok.Value;

@Value
public class Deletion {

    Long id;
    String name;

    public static <T extends Identified & Named> Deletion of(T it) {
        return new Deletion(it.getId(), it.getName());
    }

}
