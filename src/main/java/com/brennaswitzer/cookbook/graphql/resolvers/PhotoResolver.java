package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PhotoResolver implements GraphQLResolver<Photo> {

    @Autowired
    private StorageService storageService;

    public String url(Photo photo) {
        return storageService.load(photo.getObjectKey());
    }

    public float[] focus(Photo photo) {
        return photo.getFocusArray();
    }

}
