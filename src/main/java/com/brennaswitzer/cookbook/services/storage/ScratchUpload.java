package com.brennaswitzer.cookbook.services.storage;

import java.net.URL;
import java.time.OffsetDateTime;

public interface ScratchUpload {

    String filename();

    URL url();

    OffsetDateTime expiration();

    String contentType();

    String cacheControl();

    default String curl() {
        return String.format(
                "curl '%s' --header 'content-type: %s' --header 'cache-control: %s' --upload-file '%s' ",
                url(),
                contentType(),
                cacheControl(),
                filename());
    }

}
