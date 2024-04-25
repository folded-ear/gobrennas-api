package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@Slf4j
public class EmptyTrashBins {

    @Autowired
    private PlanItemRepository planItemRepository;

    @Scheduled(cron = "37 37 * * * *")
    public void emptyTrashBins() {
        val cutoff = Instant.now().minus(1, ChronoUnit.DAYS);
        val n = planItemRepository.deleteByUpdatedAtBeforeAndTrashBinIsNotNull(cutoff);
        log.info("Deleted {} old item(s) from the trash bin", n);
    }
}
