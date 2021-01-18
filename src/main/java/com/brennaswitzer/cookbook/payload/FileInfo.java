package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.services.StorageService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileInfo {

    public static FileInfo fromS3File(S3File file, StorageService storage) {
        FileInfo info = new FileInfo();
        String url = storage.load(file.getObjectKey());
        info.setUrl(url);
        int i = url.lastIndexOf("/");
        if (i > 0 && i < url.length() - 1) {
            info.setFilename(url.substring(i + 1));
        }
        info.setContentType(file.getContentType());
        info.setSize(file.getSize());
        return info;
    }

    String filename;
    String url;
    String contentType;
    Long size;

}
