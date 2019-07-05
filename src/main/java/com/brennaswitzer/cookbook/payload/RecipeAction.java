package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.services.RecipeService;

public class RecipeAction {

    public enum Type {
        RAW_INGREDIENTS_TO_LIST,
        PURCHASABLE_SCHMANKIES_TO_LIST,
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
            case RAW_INGREDIENTS_TO_LIST:
                service.addRawIngredientsToList(recipeId, getListId(), true);
                break;
            case PURCHASABLE_SCHMANKIES_TO_LIST:
                service.addPurchasableSchmankiesToList(recipeId, getListId(), true);
                break;
        }
    }

}
