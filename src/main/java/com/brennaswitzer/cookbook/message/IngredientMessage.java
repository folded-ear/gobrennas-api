package com.brennaswitzer.cookbook.message;

import com.brennaswitzer.cookbook.payload.IngredientInfo;

public class IngredientMessage {

    private String type;
    private Long id;
    private IngredientInfo info;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IngredientInfo getInfo() {
        return info;
    }

    public void setInfo(IngredientInfo info) {
        this.info = info;
    }

}
