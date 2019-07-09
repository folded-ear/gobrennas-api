package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.services.RecipeService;

public class RecipeAction {

    public enum Type {
        INGREDIENTS_TO_LIST,
    }

    private Type type;

    private Long listId;

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

    public void execute(Long recipeId, RecipeService service) {
        switch (getType()) {
            case INGREDIENTS_TO_LIST:
                service.addIngredientsToList(recipeId, getListId(), true);
                break;
        }
    }

}
