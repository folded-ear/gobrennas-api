package com.brennaswitzer.cookbook.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    void init();

    /**
     * I take a MultiportFile object and return a reference string after it was stored
     * @return reference to stored file
     */
    default String store(MultipartFile file) throws IOException {
        return store(file, file.getOriginalFilename());
    }

    /**
     * I take a MultipartFile object and a string filename/key and write to storage
     * @param filename String filename for storage
     * @return reference to the stored file
     */
    String store(MultipartFile file, String filename) throws IOException;

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
    void remove(String ref) throws IOException;

}
