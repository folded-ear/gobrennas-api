package com.brennaswitzer.cookbook.payload;

import lombok.Data;

@Data
public class ItemToRecognize {

    private String raw;

    private Integer cursor;

    public Integer getCursor() {
        return cursor == null ? raw.length() : cursor;
    }

}
