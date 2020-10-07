package com.brennaswitzer.cookbook.services;

import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class LocalStorageService implements StorageService {

    @Override
    public void init() { }

    @Override
    public String store(MultipartFile file) throws IOException {
        Assert.notNull(file, "File is required.");
        return "images/pork_chops.jpg";
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        return null;
    }

    @Override
    public String load(String filename) {
        Assert.notNull(filename, "Filename is required");
        return "images/pork_chops.jpg";
    }
}
