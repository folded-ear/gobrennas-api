package com.brennaswitzer.cookbook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.graphql")
@Getter
@Setter
public class GraphqlProperties {

    private boolean failOnUnmapped;

}
