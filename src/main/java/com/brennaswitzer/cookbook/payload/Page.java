package com.brennaswitzer.cookbook.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Page<E> {

    private int page;
    private int pageSize;
    private boolean first;
    private boolean last;
    private List<E> content;

}
