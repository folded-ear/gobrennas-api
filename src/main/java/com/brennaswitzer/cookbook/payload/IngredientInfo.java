package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IngredientInfo extends CoreRecipeInfo {

    private String type;
    private Integer storeOrder;
    private String externalUrl;
    private List<SectionInfo> sections;
    private Long ownerId;
    private Integer yield;
    private Integer calories;
    private Integer totalTime;
    private String photo;
    private List<Float> photoFocus;
    private Boolean cookThis;

    public boolean isCookThis() {
        return cookThis != null && cookThis;
    }

    public boolean hasSections() {
        return ValueUtils.hasValue(getSections());
    }

}
