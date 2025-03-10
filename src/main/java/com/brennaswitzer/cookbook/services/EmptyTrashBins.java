package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@Slf4j
public class EmptyTrashBins {

    @Autowired
    private PlanItemRepository planItemRepository;

    @Scheduled(cron = "${random.int[60]} ${random.int[60]} * * * *")
    public void emptyTrashBins() {
        val cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        var watch = new StopWatch();
        watch.start();
        val n = planItemRepository.deleteByUpdatedAtBeforeAndTrashBinIsNotNull(cutoff);
        watch.stop();
        log.info("Deleted {} old item(s) from the trash bin in {} ms", n, watch.getTotalTimeMillis());
    }
}
