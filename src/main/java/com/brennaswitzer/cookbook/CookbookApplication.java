package com.brennaswitzer.cookbook;

import com.brennaswitzer.cookbook.config.AWSProperties;
import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.config.CalendarProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableConfigurationProperties({
        AppProperties.class,
        AWSProperties.class,
        CalendarProperties.class })
public class CookbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookbookApplication.class, args);
    }

    /**
     * After Spring starts, force an immediate GC. Unclear if this will matter,
     * but <a href="https://spring.io/blog/2015/12/10/spring-boot-memory-performance">this
     * Spring article</a> seems to suggest an explicit GC will reset to a lower
     * set-point than the app ends up at initially.
     */
    @EventListener
    void onStarted(ApplicationStartedEvent ignored) {
        Runtime.getRuntime().gc();
    }

}
