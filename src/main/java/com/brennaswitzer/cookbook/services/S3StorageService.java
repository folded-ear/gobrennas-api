package com.brennaswitzer.cookbook.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class S3StorageService implements StorageService {

    private final AmazonS3 client;

    private final String region;

    private final String bucketName;

    public S3StorageService(AmazonS3 s3client, String region, String bucketName) {
        Assert.notNull(s3client, "client is required");
        Assert.notNull(region, "region is required");
        Assert.notNull(bucketName, "bucketName is required");
        this.client = s3client;
        this.region = region;
        this.bucketName = bucketName;
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
        Assert.notNull(objectKey, "objectKey is required");
        return "https://s3-" + region + ".amazonaws.com" + "/" + bucketName + "/" + objectKey;
    }

    private void put(MultipartFile file, String objectKey) throws IOException {
        ObjectMetadata md = new ObjectMetadata();
        md.setContentType(file.getContentType());
        md.setCacheControl("public");
        client.putObject(
                bucketName,
                objectKey,
                file.getInputStream(),
                md
        );
    }
}
