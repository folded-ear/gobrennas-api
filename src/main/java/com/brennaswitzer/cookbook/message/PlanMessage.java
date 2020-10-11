package com.brennaswitzer.cookbook.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PlanMessage {

    private String type;
    private Long id;
    private Object info;
    private Map<Long, Object> newIds;

}
