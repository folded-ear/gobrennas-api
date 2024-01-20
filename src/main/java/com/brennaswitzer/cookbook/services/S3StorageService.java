package com.brennaswitzer.cookbook.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.brennaswitzer.cookbook.domain.Upload;
import lombok.SneakyThrows;
import org.springframework.util.Assert;

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
    public String store(Upload upload, String objectKey) {
        Assert.notNull(upload, "upload is required.");
        Assert.notNull(objectKey, "objectKey is required.");
        put(upload, objectKey);
        return objectKey;
    }

    @Override
    public String load(String objectKey) {
        Assert.notNull(objectKey, "objectKey is required");
        return "https://s3-" + region + ".amazonaws.com" + "/" + bucketName + "/" + objectKey;
    }

    @SneakyThrows
    private void put(Upload upload, String objectKey) {
        ObjectMetadata md = new ObjectMetadata();
        md.setContentType(upload.getContentType());
        // by fiat, S3-stored assets will never change w/in a single day. :)
        md.setCacheControl("public, max-age=86400, immutable");
        md.setContentLength(upload.getSize());
        try (var is = upload.getInputStream()) {
            client.putObject(bucketName, objectKey, is, md);
        }
    }

    @Override
    public void remove(String objectKey) {
        Assert.notNull(objectKey, "objectKey is required");
        client.deleteObject(bucketName, objectKey);
    }

}
