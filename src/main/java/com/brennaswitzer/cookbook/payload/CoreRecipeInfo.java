package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Identified;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class CoreRecipeInfo implements Identified {

    private Long id;
    private String name;
    private String directions;
    private List<IngredientRefInfo> ingredients;
    private List<String> labels;

    public List<String> getLabels() {
        return labels == null
                ? Collections.emptyList()
                : labels;
    }

    public boolean hasIngredients() {
        return getIngredients() != null;
    }

}
