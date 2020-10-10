package com.brennaswitzer.cookbook.message;

import com.brennaswitzer.cookbook.payload.IngredientInfo;
import lombok.Data;

@Data
public class IngredientMessage {

    private String type;
    private Long id;
    private IngredientInfo info;

}
