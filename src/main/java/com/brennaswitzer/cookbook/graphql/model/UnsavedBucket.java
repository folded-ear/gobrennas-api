package com.brennaswitzer.cookbook.graphql.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UnsavedBucket {

    String name;
    LocalDate date;

}
