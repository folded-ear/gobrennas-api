package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * I delete Textract jobs that have gone untouched for too long, along with
 * their photos.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteOldTextractJobs {

    private final AppProperties appProperties;

    private final TextractJobRepository jobRepository;

    private final DeleteTextractJob deleteTextractJob;

    @Scheduled(cron = "${random.int[60]} ${random.int[60]} * * * *")
    public void deleteOldJobs() {
        Instant cutoff = Instant.now()
                .minus(appProperties.getDaysTextractJobIdle(),
                       ChronoUnit.DAYS);
        deleteJobsUpdatedBefore(cutoff);
    }

    /**
     * I delete every job last updated before the cutoff. A job I fail to
     * delete doesn't stop me deleting the rest.
     */
    public void deleteJobsUpdatedBefore(Instant cutoff) {
        var watch = new StopWatch();
        watch.start();
        var deleted = 0;
        var failed = 0;
        for (TextractJob job : jobRepository.findAllByUpdatedAtBefore(cutoff)) {
            long id = job.getId();
            try {
                if (deleteTextractJob.deleteIfStale(id, cutoff).isPresent()) {
                    deleted++;
                }
            } catch (RuntimeException e) {
                log.warn("Failed to delete old Textract job {}", id, e);
                failed++;
            }
        }
        watch.stop();
        log.info("Deleted {} old Textract job(s), and failed on {}, in {} ms",
                 deleted,
                 failed,
                 watch.getTotalTimeMillis());
    }

}
