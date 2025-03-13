package com.brennaswitzer.cookbook.services.storage;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.S3File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ScratchSpace {

    private static final String OBJECT_KEY_PREFIX = "scratch/";

    @Autowired
    private StorageService storageService;

    public ScratchUpload newScratchFile(Identified user,
                                        String contentType,
                                        String originalFilename) {
        return storageService.getScratchUpload(
                contentType,
                buildObjectKey(user, originalFilename),
                Duration.ofMinutes(5));
    }

    public ScratchFileDetails verifyUpload(Identified user,
                                           String filename) {
        verifyObjectKeyOwnership(user, filename);
        return new ScratchFileDetails() {

            private final FileDetails delegate = storageService.getFileDetails(filename);
            private boolean gone;

            @Override
            public String filename() {
                if (gone) throw new IllegalStateException("This scratch file no longer exists");
                return delegate.filename();
            }

            @Override
            public String contentType() {
                return delegate.contentType();
            }

            @Override
            public long size() {
                return delegate.size();
            }

            @Override
            public void moveTo(String targetFilename) {
                storageService.copy(filename, targetFilename);
                remove();
            }

            @Override
            public void remove() {
                storageService.remove(filename);
                gone = true;
            }

        };
    }

    private String buildObjectKey(Identified user,
                                  String originalFilename) {
        return OBJECT_KEY_PREFIX
               + user.getId()
               + "/"
               + LocalDate.now().getYear()
               + "/"
               + UUID.randomUUID()
               + "/"
               + S3File.sanitizeFilename(originalFilename);
    }

    private void verifyObjectKeyOwnership(Identified user,
                                          String filename) {
        if (!filename.startsWith(OBJECT_KEY_PREFIX))
            throw new IllegalArgumentException("Malformed filename");
        var s = OBJECT_KEY_PREFIX.length();
        var e = filename.indexOf('/', s);
        if (e < s)
            throw new IllegalArgumentException(String.format(
                    "Invalid filename: '%s'",
                    filename));
        var id = filename.substring(s, e);
        if (!id.equals(user.getId().toString()))
            throw new IllegalArgumentException(String.format(
                    "User '%s' is not owner of '%s'",
                    user.getId(),
                    filename));
    }

}
