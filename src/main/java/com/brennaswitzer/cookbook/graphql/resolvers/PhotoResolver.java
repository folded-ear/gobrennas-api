package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PhotoResolver {

    @Autowired
    private StorageService storageService;

    @SchemaMapping
    public String url(Photo photo) {
        return storageService.load(photo.getObjectKey());
    }

    @SchemaMapping
    public float[] focus(Photo photo) {
        return photo.getFocusArray();
    }

}
