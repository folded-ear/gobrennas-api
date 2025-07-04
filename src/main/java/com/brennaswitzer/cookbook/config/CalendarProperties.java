package com.brennaswitzer.cookbook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.calendar")
@Getter
@Setter
public class CalendarProperties {

    private boolean validate;

    /**
     * Items deleted within this many hours of creation will be marked canceled,
     * under assumption they were tentatively added and then discarded.
     */
    private int hoursDeletedWithinToCancel = 6;

}
