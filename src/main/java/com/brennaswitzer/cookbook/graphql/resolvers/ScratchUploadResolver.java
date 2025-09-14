package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.services.storage.ScratchUpload;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ScratchUploadResolver {

    @SchemaMapping
    public String curl(ScratchUpload upload) {
        return upload.curl();
    }

}
