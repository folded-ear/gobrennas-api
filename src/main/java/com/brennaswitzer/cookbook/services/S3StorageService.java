package com.brennaswitzer.cookbook.services;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class S3StorageService implements StorageService {

    private final String S3_URL = "https://s3-us-west-2.amazonaws.com";
    private final AmazonS3 client;

    @Value("${app.bucket-name}")
    String bucketName;

    public S3StorageService(AmazonS3 s3client) {
        this.client = s3client;
    }

    @Override
    public void init() {}

    @Override
    public String store(MultipartFile file) throws IOException {
        Assert.notNull(file, "File is required.");
        String objectKey = file.getOriginalFilename();
        put(file, objectKey);
        return objectKey;
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        Assert.notNull(file, "File is required.");
        Assert.notNull(filename, "Filename is required.");
        put(file, filename);
        return filename;
    }

    @Override
    public String load(String objectKey) {
        Assert.notNull(objectKey, "Filename is required");
        return  S3_URL + "/" + bucketName + "/" + objectKey;
    }

    private void put(MultipartFile file, String objectKey) throws IOException {
        File temp = File.createTempFile("recipe_", "img");
        file.transferTo(temp);
        try {
            client.putObject(
                    bucketName,
                    objectKey,
                    temp
            );
        } finally {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }
    }
}
