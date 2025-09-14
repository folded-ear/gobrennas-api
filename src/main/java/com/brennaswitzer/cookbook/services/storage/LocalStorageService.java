package com.brennaswitzer.cookbook.services.storage;

import org.springframework.util.Assert;

public class LocalStorageService implements StorageService {

    @Override
    public String copy(String source, String dest) {
        Assert.notNull(source, "source is required.");
        Assert.notNull(dest, "dest is required.");
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
