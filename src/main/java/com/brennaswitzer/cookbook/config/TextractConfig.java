package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.services.textract.AWSTextractProvider;
import com.brennaswitzer.cookbook.services.textract.TextractProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class TextractConfig {

    @Profile({"production", "default"})
    @Bean
    public TextractProvider awsProvider(AWSProperties awsProps, TextractClient textractClient) {
        return new AWSTextractProvider(textractClient, awsProps.getBucketName());
    }

    @Profile("test")
    @Bean
    public TextractProvider noOpProvider() {
        return photo -> {
            throw new UnsupportedOperationException("yeah, um, no?");
        };
    }

}
