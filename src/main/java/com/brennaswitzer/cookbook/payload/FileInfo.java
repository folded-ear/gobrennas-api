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
        info.setUrl(storage.load(file.getObjectKey()));
        info.setFilename(file.getFilename());
        info.setContentType(file.getContentType());
        info.setSize(file.getSize());
        return info;
    }

    String filename;
    String url;
    String contentType;
    Long size;

}
