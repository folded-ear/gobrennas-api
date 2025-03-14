package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.services.UnitLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;

@Configuration
@Slf4j
public class UpgradeConfig {

    @Autowired(required = false)
    private UnitLoader unitLoader;

    /**
     * After Spring starts, force an immediate GC. Unclear if this will matter,
     * but <a href="https://spring.io/blog/2015/12/10/spring-boot-memory-performance">this
     * Spring article</a> seems to suggest an explicit GC will reset to a lower
     * set-point than the app ends up at initially.
     */
    @EventListener(ApplicationStartedEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void gcOnStart() {
        logMem("Before GC");
        Runtime.getRuntime().gc();
        logMem("After GC");
    }

    private void logMem(String label) {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        long percent = used * 100 / total;
        if (max == Long.MAX_VALUE || max == 0x100000000L) {
            log.info("{}: {}MB of {}MB ({}%) used (unbounded)",
                     label,
                     used / 1048576L,
                     total / 1048576L,
                     percent);

        } else {
            log.info("{}: {}MB of {}MB ({}%) used ({}MB max)",
                     label,
                     used / 1048576L,
                     total / 1048576L,
                     percent,
                     max / 1048576L);
        }
    }

    @EventListener(ApplicationStartedEvent.class)
    @Async
    public void loadUnits() {
        if (unitLoader == null) return;
        try {
            unitLoader.loadUnits("units.yml");
        } catch (Exception e) {
            log.error("Error loading units", e);
        }
    }

}
