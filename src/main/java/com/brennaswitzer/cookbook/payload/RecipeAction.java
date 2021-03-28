package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.services.RecipeService;
import lombok.Getter;
import lombok.Setter;

public class RecipeAction {

    public enum Type {
        SEND_TO_PLAN, // new parent for recipe w/ ingredients nested
        RECOGNIZE_ITEM, // todo: remove
    }

    @Getter @Setter
    private Type type;

    @Getter @Setter
    private Long planId;

    @Getter @Setter
    private RawIngredientDissection dissection;

    @Getter @Setter
    private String raw;

    @Getter @Setter
    private Integer cursorPosition;

    @Deprecated
    public Long getListId() {
        return planId;
    }

    @Deprecated
    public void setListId(Long listId) {
        this.planId = listId;
    }

    public Object execute(RecipeService service) {// todo: go away?
        //noinspection SwitchStatementWithTooFewBranches
        switch (getType()) {
            case RECOGNIZE_ITEM: // todo: go away
                return service.recognizeItem(raw, cursorPosition == null ? raw.length() : cursorPosition);
            default:
                throw new UnsupportedOperationException("Can't process " + getType());
        }
    }

    public Object execute(Long recipeId, RecipeService service) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (getType()) {
            case SEND_TO_PLAN:
                service.sendToPlan(recipeId, planId);
                return true;
            default:
                throw new UnsupportedOperationException("Can't process " + getType());
        }
    }

}
