package com.brennaswitzer.cookbook.graphql.model;

import lombok.Data;

@Data
public class SuggestionRequest {

    int first;

    OffsetConnectionCursor after;

}
