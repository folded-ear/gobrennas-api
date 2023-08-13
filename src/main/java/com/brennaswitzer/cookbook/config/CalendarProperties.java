package com.brennaswitzer.cookbook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.calendar")
@Getter
@Setter
public class CalendarProperties {

    private boolean validate;

}
