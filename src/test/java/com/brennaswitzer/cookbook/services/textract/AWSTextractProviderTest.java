package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.ApiCallAttemptTimeoutException;
import software.amazon.awssdk.core.exception.ApiCallTimeoutException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.AccessDeniedException;
import software.amazon.awssdk.services.textract.model.BadDocumentException;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.BoundingBox;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.DocumentTooLargeException;
import software.amazon.awssdk.services.textract.model.Geometry;
import software.amazon.awssdk.services.textract.model.InternalServerErrorException;
import software.amazon.awssdk.services.textract.model.InvalidS3ObjectException;
import software.amazon.awssdk.services.textract.model.ProvisionedThroughputExceededException;
import software.amazon.awssdk.services.textract.model.TextractException;
import software.amazon.awssdk.services.textract.model.ThrottlingException;
import software.amazon.awssdk.services.textract.model.UnsupportedDocumentException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AWSTextractProviderTest {

    private static final S3File PHOTO = new S3File(
            "textract/1/2/photo.jpg",
            "image/jpeg",
            1L);

    @Mock
    private TextractClient client;

    private AWSTextractProvider provider;

    @BeforeEach
    void setUp() {
        provider = new AWSTextractProvider(client, "bucket");
    }

    @Test
    void lineBlocksBecomeLines() {
        var box = BoundingBox.builder()
                .left(0.1f)
                .top(0.2f)
                .width(0.3f)
                .height(0.4f)
                .build();
        var geometry = Geometry.builder()
                .boundingBox(box)
                .build();
        when(client.detectDocumentText(any(DetectDocumentTextRequest.class)))
                .thenReturn(DetectDocumentTextResponse.builder()
                                    .blocks(block(BlockType.PAGE, null, geometry),
                                            block(BlockType.LINE, "one cup flour", geometry),
                                            block(BlockType.WORD, "one", geometry))
                                    .build());

        Set<TextractJob.Line> lines = provider.detectLines(PHOTO);

        assertEquals(
                Set.of(new TextractJob.Line(
                        "one cup flour",
                        new TextractJob.Box(box.left(),
                                            box.top(),
                                            box.width(),
                                            box.height()))),
                lines);
    }

    @ParameterizedTest
    @MethodSource
    void retryable(SdkException thrown) {
        assertTrue(failureFrom(thrown).isRetryable());
    }

    static List<SdkException> retryable() {
        return List.of(
                clientException(new SocketTimeoutException("Read timed out")),
                clientException(new UncheckedIOException(new IOException("Connection reset"))),
                clientException(new RuntimeException(new IOException("Broken pipe"))),
                ApiCallTimeoutException.create(30_000),
                ApiCallAttemptTimeoutException.create(30_000),
                serviceException(ThrottlingException.builder(), 400, "ThrottlingException"),
                serviceException(ProvisionedThroughputExceededException.builder(), 400, "ProvisionedThroughputExceededException"),
                serviceException(TextractException.builder(), 429, "SomethingNew"),
                serviceException(InternalServerErrorException.builder(), 500, "InternalServerError"),
                serviceException(TextractException.builder(), 503, "ServiceUnavailable"));
    }

    @ParameterizedTest
    @MethodSource
    void notRetryable(SdkException thrown) {
        assertFalse(failureFrom(thrown).isRetryable());
    }

    static List<SdkException> notRetryable() {
        return List.of(
                SdkClientException.create("Unable to load credentials"),
                clientException(new IllegalStateException("Unable to marshall request")),
                serviceException(BadDocumentException.builder(), 400, "BadDocumentException"),
                serviceException(UnsupportedDocumentException.builder(), 400, "UnsupportedDocumentException"),
                serviceException(DocumentTooLargeException.builder(), 400, "DocumentTooLargeException"),
                serviceException(InvalidS3ObjectException.builder(), 400, "InvalidS3ObjectException"),
                serviceException(AccessDeniedException.builder(), 400, "AccessDeniedException"),
                SdkException.create("Something else entirely", null));
    }

    @Test
    void serviceFailureCarriesAwsErrorMessage() {
        var thrown = BadDocumentException.builder()
                .statusCode(400)
                .requestId("abc-123")
                .message("Request has unsupported document format (Service: Textract, Status Code: 400, Request ID: abc-123)")
                .awsErrorDetails(AwsErrorDetails.builder()
                                         .errorCode("BadDocumentException")
                                         .errorMessage("Request has unsupported document format")
                                         .build())
                .build();

        TextractFailure failure = failureFrom(thrown);

        assertEquals("Request has unsupported document format", failure.getMessage());
        assertSame(thrown, failure.getCause());
    }

    @Test
    void clientFailureCarriesItsMessage() {
        var thrown = clientException(new SocketTimeoutException("Read timed out"));

        TextractFailure failure = failureFrom(thrown);

        assertEquals(thrown.getMessage(), failure.getMessage());
        assertSame(thrown, failure.getCause());
    }

    @Test
    void blankMessageIsUnknown() {
        var thrown = serviceException(InternalServerErrorException.builder(), 500, "InternalServerError")
                .toBuilder()
                .awsErrorDetails(AwsErrorDetails.builder()
                                         .errorCode("InternalServerError")
                                         .errorMessage(" ")
                                         .build())
                .build();

        assertEquals(TextractFailure.UNKNOWN_ERROR, failureFrom(thrown).getMessage());
    }

    @Test
    void missingErrorDetailsIsUnknown() {
        var thrown = InternalServerErrorException.builder()
                .statusCode(500)
                .build();

        assertEquals(TextractFailure.UNKNOWN_ERROR, failureFrom(thrown).getMessage());
    }

    private TextractFailure failureFrom(SdkException thrown) {
        when(client.detectDocumentText(any(DetectDocumentTextRequest.class)))
                .thenThrow(thrown);
        return assertThrows(TextractFailure.class,
                            () -> provider.detectLines(PHOTO));
    }

    private static Block block(BlockType type, String text, Geometry geometry) {
        return Block.builder()
                .blockType(type)
                .text(text)
                .geometry(geometry)
                .build();
    }

    private static SdkClientException clientException(Throwable cause) {
        return SdkClientException.builder()
                .message("Unable to execute HTTP request: " + cause.getMessage())
                .cause(cause)
                .build();
    }

    private static AwsServiceException serviceException(AwsServiceException.Builder builder,
                                                        int statusCode,
                                                        String errorCode) {
        return builder
                .statusCode(statusCode)
                .awsErrorDetails(AwsErrorDetails.builder()
                                         .errorCode(errorCode)
                                         .errorMessage("Something went wrong")
                                         .build())
                .build();
    }

}
