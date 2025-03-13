package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Upload;
import lombok.SneakyThrows;
import org.springframework.util.Assert;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3StorageService implements StorageService {

    private final S3Client client;

    private final String region;

    private final String bucketName;

    public S3StorageService(S3Client s3client, String region, String bucketName) {
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
    public String copy(String source, String dest) {
        Assert.notNull(source, "source is required.");
        Assert.notNull(dest, "dest is required.");
        client.copyObject(CopyObjectRequest.builder()
                                  .sourceBucket(bucketName)
                                  .sourceKey(source)
                                  .destinationBucket(bucketName)
                                  .destinationKey(dest)
                                  .build());
        return dest;
    }

    @Override
    public String load(String objectKey) {
        Assert.notNull(objectKey, "objectKey is required");
        return "https://s3-" + region + ".amazonaws.com" + "/" + bucketName + "/" + objectKey;
    }

    @SneakyThrows
    private void put(Upload upload, String objectKey) {
        var req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(upload.getContentType())
                // by fiat, S3-stored assets will never change w/in a single day. :)
                .cacheControl("public, max-age=86400, immutable")
                .build();
        try (var is = upload.getInputStream()) {
            client.putObject(
                    req,
                    RequestBody.fromInputStream(
                            is,
                            upload.getSize()));
        }
    }

    @Override
    public void remove(String objectKey) {
        Assert.notNull(objectKey, "objectKey is required");
        client.deleteObject(DeleteObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(objectKey)
                                    .build());
    }

}
