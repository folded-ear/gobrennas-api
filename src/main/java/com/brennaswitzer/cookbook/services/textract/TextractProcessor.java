package com.brennaswitzer.cookbook.services.textract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TextractProcessor {

    @Autowired
    private TextractProvider textractProvider;

    @EventListener
    @Async
    public void processJob(JobCreatedEvent event) {
        textractProvider.processJob(event.getId());
    }

}
