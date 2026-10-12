package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.services.storage.LocalStorageService;

import java.util.HashSet;
import java.util.Set;

/**
 * I am an in-memory store of object keys, some of which I can be told to
 * refuse to remove.
 */
class FakeStorage extends LocalStorageService {

    private final Set<String> objectKeys = new HashSet<>();

    private final Set<String> unremovable = new HashSet<>();

    void put(String objectKey) {
        objectKeys.add(objectKey);
    }

    boolean holds(String objectKey) {
        return objectKeys.contains(objectKey);
    }

    void refuseToRemove(String objectKey) {
        unremovable.add(objectKey);
    }

    @Override
    public void remove(String objectKey) {
        if (unremovable.contains(objectKey)) {
            throw new IllegalStateException(
                    "Refusing to remove '" + objectKey + "'");
        }
        objectKeys.remove(objectKey);
    }

}
