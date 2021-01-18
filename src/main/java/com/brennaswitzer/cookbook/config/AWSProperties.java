package com.brennaswitzer.cookbook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
@Getter
@Setter
public class AWSProperties {
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
