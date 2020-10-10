package com.brennaswitzer.cookbook.message;

import lombok.Data;

@Data
public class PlanMessage {

    private String type;
    private Long id;
    private Object info;

}
