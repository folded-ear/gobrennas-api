package com.brennaswitzer.cookbook.services.storage;

import com.brennaswitzer.cookbook.domain.Upload;
import lombok.SneakyThrows;
import org.springframework.util.Assert;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class S3StorageService implements StorageService {

    private record S3ScratchUpload(
            URL url,
            OffsetDateTime expiration,
            String contentType,
            String cacheControl,
            String bucketName,
            String filename) implements ScratchUpload {}

    private record S3FileDetails(
            S3StorageService service,
            String contentType,
            String bucketName,
            String filename,
            long size) implements FileDetails {}

    private final S3Client client;

    private final S3Presigner presigner;

    private final String bucketName;

    public S3StorageService(S3Client client,
                            S3Presigner presigner,
                            String bucketName) {
        Assert.notNull(client, "client is required");
        Assert.notNull(presigner, "pre-signer is required");
        Assert.notNull(bucketName, "bucketName is required");
        this.client = client;
        this.presigner = presigner;
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
    public ScratchUpload getScratchUpload(String contentType,
                                          String objectKey,
                                          Duration expiry) {
        PutObjectRequest req = buildPutRequest(objectKey, contentType);
        PresignedPutObjectRequest presigned = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(expiry)
                        .putObjectRequest(req)
                        .build());
        return new S3ScratchUpload(
                presigned.url(),
                presigned.expiration()
                        .atOffset(ZoneOffset.UTC),
                contentType,
                req.cacheControl(),
                bucketName,
                objectKey);
    }

    @Override
    public FileDetails getFileDetails(String objectKey) {
        try {
            HeadObjectResponse resp = client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build());
            return new S3FileDetails(this,
                                     resp.contentType(),
                                     bucketName,
                                     objectKey,
                                     resp.contentLength());
        } catch (NoSuchKeyException nske) {
            throw new IllegalArgumentException(
                    String.format("Unknown file '%s'",
                                  objectKey),
                    nske);
        }
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
        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        return client.utilities()
                .getUrl(request)
                .toExternalForm();
    }

    @SneakyThrows
    private void put(Upload upload, String objectKey) {
        try (var is = upload.getInputStream()) {
            client.putObject(
                    buildPutRequest(objectKey, upload.getContentType()),
                    RequestBody.fromInputStream(is, upload.getSize()));
        }
    }

    private PutObjectRequest buildPutRequest(String objectKey,
                                             String contentType) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                // by fiat, S3-stored assets will never change w/in a single day. :)
                .cacheControl("public, max-age=86400, immutable")
                .build();
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
