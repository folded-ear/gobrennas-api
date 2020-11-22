package com.brennaswitzer.cookbook.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PlanMessage {

    private String type;
    private Long id;
    private Object info;
    private Map<Long, Object> newIds;

    public void addNewId(Long newId, Object oldId) {
        if (newIds == null) {
            newIds = new HashMap<>();
        }
        newIds.put(newId, oldId);
    }

}
