package com.brennaswitzer.cookbook.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    void init();
    /**
     * I take a File object and return a reference string after it was stored
     * @return reference to file that was stored
     */
    String store(MultipartFile file) throws IOException;

    String load(String filename);

}
