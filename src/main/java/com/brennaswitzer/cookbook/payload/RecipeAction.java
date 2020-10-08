package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.services.RecipeService;

import java.util.List;

public class RecipeAction {

    public enum Type {
        SEND_TO_PLAN, // new parent for recipe w/ ingredients nested
        DISSECT_RAW_INGREDIENT,
        RECOGNIZE_ITEM,
    }

    private Type type;

    private Long listId;

    private List<Long> additionalRecipeIds;

    private RawIngredientDissection dissection;

    private String raw;

    private Integer cursorPosition;

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

    public Long getPlanId() {
        return listId;
    }

    public void setPlanId(Long planId) {
        this.listId = planId;
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

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public Integer getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(Integer cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    public Object execute(RecipeService service) {
        switch (getType()) {
            case DISSECT_RAW_INGREDIENT:
                service.recordDissection(dissection);
                break;
            case RECOGNIZE_ITEM:
                //noinspection deprecation
                return service.recognizeItem(raw, cursorPosition == null ? raw.length() : cursorPosition);
            default:
                throw new UnsupportedOperationException("Can't process " + getType());
        }
        return true;
    }

    public Object execute(Long recipeId, RecipeService service) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (getType()) {
            case SEND_TO_PLAN:
                service.sendToPlan(recipeId, getPlanId());
                break;
            default:
                throw new UnsupportedOperationException("Can't process " + getType());
        }
        return true;
    }

}
