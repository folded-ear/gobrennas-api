package com.brennaswitzer.cookbook.services;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.BoundingBox;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;
import com.brennaswitzer.cookbook.config.AWSProperties;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AWSTextractService {

    @Autowired
    private AmazonTextract textractClient;

    @Autowired
    private TextractJobRepository jobRepository;

    @Autowired
    private AWSProperties awsProps;

    public void processJob(long jobId) {
        TextractJob job = jobRepository.getOne(jobId);
        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                .withDocument(new Document()
                        .withS3Object(new S3Object()
                                .withBucket(awsProps.getBucketName())
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
