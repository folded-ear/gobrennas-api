package com.brennaswitzer.cookbook.services.storage;

import java.time.Duration;

public interface StorageService {

    /**
     * I construct and return a {@link ScratchUpload} which can be used to
     * anonymously upload content to the given location (optional operation).
     *
     * @param contentType MIME type to be stored
     * @param filename    filename for storage
     * @param expiry      how long the URL is valid for
     * @throws UnsupportedOperationException if this instance doesn't support
     *                                       pre-signed store URLs.
     */
    default ScratchUpload getScratchUpload(String contentType,
                                           String filename,
                                           Duration expiry) {
        throw new UnsupportedOperationException();
    }

    /**
     * I verify a file exists and return details about it.
     *
     * @param filename filename for storage
     * @throws RuntimeException              if verification fails.
     * @throws UnsupportedOperationException if this instance doesn't support
     *                                       file details.
     */
    default FileDetails getFileDetails(String filename) {
        throw new UnsupportedOperationException();
    }

    /**
     * I take two string filename/keys and copy the content of the first to the
     * second. A complete copy is made, no backreferences are created.
     *
     * @param source filename to copy from
     * @param dest   filename to copy to
     * @return reference to the stored (destination) file
     */
    String copy(String source, String dest);

    /**
     * I return a fully-qualified URL for the passed file reference.
     * @param ref A reference to a stored file.
     * @return The url of the stored file.
     */
    String load(String ref);

    /**
     * I remove the passed file reference from the store.
     * @param ref A reference to a stored file to remove.
     */
    void remove(String ref);

}
