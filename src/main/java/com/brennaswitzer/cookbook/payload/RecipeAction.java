package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.services.RecipeService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RecipeAction {

    public enum Type {
        SEND_TO_PLAN, // new parent for recipe w/ ingredients nested
        DISSECT_RAW_INGREDIENT,
        RECOGNIZE_ITEM,
    }

    @Getter @Setter
    private Type type;

    @Getter @Setter
    private Long listId;

    @Getter @Setter
    private List<Long> additionalRecipeIds;

    @Getter @Setter
    private RawIngredientDissection dissection;

    @Getter @Setter
    private String raw;

    @Getter @Setter
    private Integer cursorPosition;

    public Long getPlanId() {
        return listId;
    }

    public void setPlanId(Long planId) {
        this.listId = planId;
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
