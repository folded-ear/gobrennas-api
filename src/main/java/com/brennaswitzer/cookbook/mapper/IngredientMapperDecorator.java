package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.services.StorageService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public abstract class IngredientMapperDecorator implements IngredientMapper {

    @Autowired
    @Qualifier("delegate")
    private IngredientMapper delegate;

    @Autowired
    private StorageService storageService;

    @Override
    public IngredientInfo recipeToInfo(Recipe r) {
        val info = delegate.recipeToInfo(r);
        if (r.getOwner() != null) {
            info.setOwnerId(r.getOwner().getId());
        }
        if (r.hasPhoto()) {
            Photo photo = r.getPhoto();
            info.setPhoto(storageService.load(photo.getObjectKey()));
            if (photo.hasFocus()) {
                info.setPhotoFocus(photo.getFocusArray());
            }
        }
        return info;
    }

}
