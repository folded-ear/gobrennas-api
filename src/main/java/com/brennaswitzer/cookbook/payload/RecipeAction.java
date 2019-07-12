package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.services.RecipeService;

public class RecipeAction {

    public enum Type {
        ASSEMBLE_SHOPPING_LIST,
        DISSECT_RAW_INGREDIENT,
    }

    private Type type;

    private Long listId;

    private RawIngredientDissection dissection;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public RawIngredientDissection getDissection() {
        return dissection;
    }

    public void setDissection(RawIngredientDissection dissection) {
        this.dissection = dissection;
    }

    public void execute(Long recipeId, RecipeService service) {
        switch (getType()) {
            case ASSEMBLE_SHOPPING_LIST:
                service.assembleShoppingList(recipeId, getListId(), true);
                break;
            case DISSECT_RAW_INGREDIENT:
                service.recordDissection(dissection);
                break;
        }
    }

}
