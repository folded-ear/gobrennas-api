package com.brennaswitzer.cookbook.services.textract;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.BoundingBox;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class AWSTextractProvider implements TextractProvider {

    private final AmazonTextract textractClient;

    private final TextractJobRepository jobRepository;

    private final String bucketName;

    public AWSTextractProvider(AmazonTextract textractClient, TextractJobRepository jobRepository, String bucketName) {
        this.textractClient = textractClient;
        this.jobRepository = jobRepository;
        this.bucketName = bucketName;
    }

    public void processJob(long jobId) {
        TextractJob job = jobRepository.getReferenceById(jobId);
        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                .withDocument(new Document()
                        .withS3Object(new S3Object()
                                .withBucket(bucketName)
                                .withName(job.getPhoto().getObjectKey())));
        Set<TextractJob.Line> lines = textractClient.detectDocumentText(request)
                .getBlocks()
                .stream()
                .filter(b -> "LINE".equals(b.getBlockType()))
                .map(b -> {
                    TextractJob.Line l = new TextractJob.Line();
                    l.setText(b.getText());
                    BoundingBox bb = b.getGeometry().getBoundingBox();
                    l.setBox(new TextractJob.Box(
                            bb.getLeft(),
                            bb.getTop(),
                            bb.getWidth(),
                            bb.getHeight()
                    ));
                    return l;
                })
                .collect(Collectors.toSet());
        job.setLines(lines);
        job.setReady(true);
    }
}
