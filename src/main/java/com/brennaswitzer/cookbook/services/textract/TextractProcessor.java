package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.TextractJobStatus;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextractProcessor {

    private final TextractProvider textractProvider;

    private final TextractJobRepository jobRepository;

    private final TransactionTemplate txTemplate;

    @EventListener
    @Async
    public void processJob(JobCreatedEvent event) {
        long jobId = event.getId();
        jobRepository.findById(jobId)
                .map(TextractJob::getPhoto)
                .map(photo -> extract(jobId, photo))
                .ifPresent(outcome -> record(jobId, outcome));
    }

    /**
     * I extract the photo's text, outside any transaction, and return what
     * that means for its job.
     */
    private Consumer<TextractJob> extract(long jobId, S3File photo) {
        try {
            Set<TextractJob.Line> lines = textractProvider.detectLines(photo);
            return job -> job.succeed(lines);
        } catch (TextractFailure f) {
            log.warn("Textract job {} failed", jobId, f);
            return job -> job.fail(f.getMessage(), f.isRetryable());
        } catch (RuntimeException e) {
            log.error("Textract job {} failed unexpectedly", jobId, e);
            return job -> job.fail(TextractFailure.UNKNOWN_ERROR, false);
        }
    }

    private void record(long jobId, Consumer<TextractJob> outcome) {
        txTemplate.executeWithoutResult(tx -> jobRepository.findById(jobId)
                .filter(job -> job.getStatus() == TextractJobStatus.PENDING)
                .ifPresent(outcome));
    }

}
