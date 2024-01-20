package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Upload;
import org.springframework.util.Assert;

public class LocalStorageService implements StorageService {

    @Override
    public String store(Upload upload, String filename) {
        Assert.notNull(upload, "upload is required.");
        Assert.notNull(filename, "filename is required.");
        return "images/pork_chops.jpg";
    }

    @Override
    public String load(String filename) {
        Assert.notNull(filename, "Filename is required");
        return "images/pork_chops.jpg";
    }

    @Override
    public void remove(String filename) {
        Assert.notNull(filename, "Filename is required");
    }

}
