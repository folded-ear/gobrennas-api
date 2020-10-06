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

        File temp = File.createTempFile("recipe_", "img");
        temp.deleteOnExit();
        file.transferTo(temp);

        // TODO: namespace key to avoid collision
        // try/catch block and then add finally so that on exit it will be removed
        String objectKey = file.getOriginalFilename();

        client.putObject(
                bucketName,
                objectKey,
                temp
        );
        temp.delete();
        return objectKey;
    }

    @Override
    public String load(String objectKey) {
        Assert.notNull(objectKey, "Filename is required");
        return  S3_URL + "/" + bucketName + "/" + objectKey;
    }
}
