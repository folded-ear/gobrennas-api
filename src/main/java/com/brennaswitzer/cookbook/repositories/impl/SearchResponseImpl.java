package com.brennaswitzer.cookbook.repositories.impl;

import com.brennaswitzer.cookbook.repositories.SearchRequest;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SearchResponseImpl<R> implements SearchResponse<R> {

    List<R> content;
    SearchRequest request;
    boolean last;

}
