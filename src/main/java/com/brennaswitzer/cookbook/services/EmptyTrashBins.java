package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class EmptyTrashBins {

    @Autowired
    private TaskRepository taskRepository;

    @Scheduled(fixedDelay = 60, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void emptyTrashBins() {
        val cutoff = Instant.now().minus(4, ChronoUnit.HOURS);
        val n = taskRepository.deleteByUpdatedAtBeforeAndTrashBinIsNotNull(cutoff);
        log.info("Deleted {} old task(s) from the trash bin", n);
    }
}
