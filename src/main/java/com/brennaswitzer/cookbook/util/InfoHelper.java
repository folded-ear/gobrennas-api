package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InfoHelper {

    @Autowired
    private StorageService storageService;

    public IngredientInfo getRecipeInfo(Recipe r) {
        IngredientInfo info = IngredientInfo.from(r);
        if(r.hasPhoto()) {
            Photo photo = r.getPhoto();
            info.setPhoto(storageService.load(photo.getObjectKey()));
            if (photo.hasFocus()) {
                info.setPhotoFocus(photo.getFocusArray());
            }
        }
        return info;
    }

}
