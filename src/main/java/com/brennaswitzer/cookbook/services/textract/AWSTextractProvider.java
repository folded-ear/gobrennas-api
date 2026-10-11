package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.util.ValueUtils;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.ApiCallAttemptTimeoutException;
import software.amazon.awssdk.core.exception.ApiCallTimeoutException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.BoundingBox;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class AWSTextractProvider implements TextractProvider {

    private final TextractClient textractClient;

    private final String bucketName;

    public AWSTextractProvider(TextractClient textractClient, String bucketName) {
        this.textractClient = textractClient;
        this.bucketName = bucketName;
    }

    @Override
    public Set<TextractJob.Line> detectLines(S3File photo) {
        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                                  .s3Object(S3Object.builder()
                                                    .bucket(bucketName)
                                                    .name(photo.getObjectKey())
                                                    .build())
                                  .build())
                .build();
        try {
            return textractClient.detectDocumentText(request)
                    .blocks()
                    .stream()
                    .filter(b -> b.blockType() == BlockType.LINE)
                    .map(b -> {
                        TextractJob.Line l = new TextractJob.Line();
                        l.setText(b.text());
                        BoundingBox bb = b.geometry().boundingBox();
                        l.setBox(new TextractJob.Box(
                                bb.left(),
                                bb.top(),
                                bb.width(),
                                bb.height()
                        ));
                        return l;
                    })
                    .collect(Collectors.toSet());
        } catch (SdkException e) {
            throw new TextractFailure(messageOf(e), isRetryable(e), e);
        }
    }

    /**
     * I say whether the failure is transient: no response for want of I/O or
     * time, throttling, or a server-side error. The SDK's own
     * {@code retryable()} flags are constant, so are no help.
     */
    private static boolean isRetryable(SdkException e) {
        if (e instanceof SdkClientException) {
            return e instanceof ApiCallTimeoutException
                   || e instanceof ApiCallAttemptTimeoutException
                   || hasIoCause(e);
        }
        if (e instanceof AwsServiceException ase) {
            return ase.isThrottlingException()
                   || ase.statusCode() >= HttpStatusCode.INTERNAL_SERVER_ERROR;
        }
        return false;
    }

    private static boolean hasIoCause(Throwable t) {
        for (var c = t.getCause(); c != null; c = c.getCause()) {
            if (c instanceof IOException) return true;
        }
        return false;
    }

    private static String messageOf(SdkException e) {
        String message = null;
        if (e instanceof AwsServiceException ase) {
            AwsErrorDetails details = ase.awsErrorDetails();
            if (details != null) message = details.errorMessage();
        } else if (e instanceof SdkClientException) {
            message = e.getMessage();
        }
        return ValueUtils.noValue(message)
                ? TextractFailure.UNKNOWN_ERROR
                : message;
    }

}
