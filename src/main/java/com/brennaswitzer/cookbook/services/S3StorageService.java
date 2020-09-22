package com.brennaswitzer.cookbook.services;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class S3StorageService implements StorageService {

    private final AmazonS3 client;

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
                "foodinger",
                objectKey,
                temp
        );
        temp.delete();
        return objectKey;
    }

    @Override
    public String load(String filename) {
        return "https://foodinger.s3-us-west-2.amazonaws.com/pork_chops.jpg";
    }
}
