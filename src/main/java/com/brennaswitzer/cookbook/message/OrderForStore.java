package com.brennaswitzer.cookbook.message;

import lombok.Data;

@Data
public class OrderForStore {

    private Long id;
    private Long targetId;
    private boolean after = true;

}
