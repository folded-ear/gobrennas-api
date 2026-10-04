package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.repositories.PlanBucketRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDate;

@Service
@Transactional
@Slf4j
public class DeleteOldBuckets {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private PlanBucketRepository planBucketRepository;

    @Scheduled(cron = "${random.int[60]} ${random.int[60]} * * * *")
    public void deleteOldBuckets() {
        val cutoff = LocalDate.now()
                .minusDays(appProperties.getDaysPastBucketDate());
        var watch = new StopWatch();
        watch.start();
        var n = 0;
        for (val bucket : planBucketRepository.findAllByDateLessThanEqual(cutoff)) {
            if (bucket.getItems()
                    .stream()
                    .allMatch(DeleteOldBuckets::isTrashed)) {
                bucket.setPlan(null);
                n++;
            }
        }
        watch.stop();
        log.info("Deleted {} old bucket(s) in {} ms", n, watch.getTotalTimeMillis());
    }

    private static boolean isTrashed(PlanItem item) {
        return item.isDirectlyInTrashBin() || item.isImplicitlyInTrashBin();
    }

}
