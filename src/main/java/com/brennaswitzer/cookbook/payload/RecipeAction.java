package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.services.RecipeService;

import java.util.List;

public class RecipeAction {

    public enum Type {
        ASSEMBLE_SHOPPING_LIST,
        DISSECT_RAW_INGREDIENT,
    }

    private Type type;

    private Long listId;

    private List<Long> additionalRecipeIds;

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

    public List<Long> getAdditionalRecipeIds() {
        return additionalRecipeIds;
    }

    public void setAdditionalRecipeIds(List<Long> additionalRecipeIds) {
        this.additionalRecipeIds = additionalRecipeIds;
    }

    public void execute(RecipeService service) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (getType()) {
            case DISSECT_RAW_INGREDIENT:
                service.recordDissection(dissection);
                break;
            default:
                throw new UnsupportedOperationException("Can't process " + getType());
        }
    }

    public void execute(Long recipeId, RecipeService service) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (getType()) {
            case ASSEMBLE_SHOPPING_LIST:
                service.assembleShoppingList(recipeId, additionalRecipeIds, getListId(), true);
                break;
            default:
                throw new UnsupportedOperationException("Can't process " + getType());
        }
    }

}
