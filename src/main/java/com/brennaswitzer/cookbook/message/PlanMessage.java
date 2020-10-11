package com.brennaswitzer.cookbook.message;

import lombok.Data;

import java.util.Map;

@Data
public class PlanMessage {

    private String type;
    private Long id;
    private Object info;
    private Map<Long, Object> newIds;

}
