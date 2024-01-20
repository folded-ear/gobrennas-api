package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Upload;

public interface StorageService {

    /**
     * I take a Upload object and a string filename/key and write to storage
     * @param filename String filename for storage
     * @return reference to the stored file
     */
    String store(Upload upload, String filename);

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
