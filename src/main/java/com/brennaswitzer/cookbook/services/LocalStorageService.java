package com.brennaswitzer.cookbook.services;

import org.springframework.web.multipart.MultipartFile;

public class LocalStorageService implements StorageService {

    @Override
    public void init() { }

    @Override
    public String store(MultipartFile file) {
        // write it to disk
        return "images/pork_chops.jpg";
    }

    @Override
    public String load(String filename) {
        return "images/pork_chops.jpg";
    }
}
