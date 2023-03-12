package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.services.StorageService;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused") // component-scanned for graphql-java
@Component
public class PhotoResolver implements GraphQLResolver<Photo> {

    @Autowired
    private StorageService storageService;

    public String getUrl(Photo photo) {
        return storageService.load(photo.getObjectKey());
    }

    public float[] getFocus(Photo photo) {
        return photo.getFocusArray();
    }

}
