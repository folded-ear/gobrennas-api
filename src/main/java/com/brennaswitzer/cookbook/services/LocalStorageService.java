package com.brennaswitzer.cookbook.services;

import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

public class LocalStorageService implements StorageService {

    @Override
    public void init() { }

    @Override
    public String store(MultipartFile file, String filename) {
        Assert.notNull(file, "File is required.");
        Assert.notNull(filename, "Filename is required.");
        return "images/pork_chops.jpg";
    }

    @Override
    public String load(String filename) {
        Assert.notNull(filename, "Filename is required");
        return "images/pork_chops.jpg";
    }
}
