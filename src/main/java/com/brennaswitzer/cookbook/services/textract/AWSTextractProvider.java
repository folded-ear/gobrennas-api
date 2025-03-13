package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.BoundingBox;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class AWSTextractProvider implements TextractProvider {

    private final TextractClient textractClient;

    private final TextractJobRepository jobRepository;

    private final String bucketName;

    public AWSTextractProvider(TextractClient textractClient, TextractJobRepository jobRepository, String bucketName) {
        this.textractClient = textractClient;
        this.jobRepository = jobRepository;
        this.bucketName = bucketName;
    }

    public void processJob(long jobId) {
        TextractJob job = jobRepository.getReferenceById(jobId);
        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                                  .s3Object(S3Object.builder()
                                                    .bucket(bucketName)
                                                    .name(job.getPhoto().getObjectKey())
                                                    .build())
                                  .build())
                .build();
        Set<TextractJob.Line> lines = textractClient.detectDocumentText(request)
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
        job.setLines(lines);
        job.setReady(true);
    }
}
