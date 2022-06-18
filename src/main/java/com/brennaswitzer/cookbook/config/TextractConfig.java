package com.brennaswitzer.cookbook.config;

import com.amazonaws.services.textract.AmazonTextract;
import com.brennaswitzer.cookbook.repositories.TextractJobRepository;
import com.brennaswitzer.cookbook.services.AWSTextractProvider;
import com.brennaswitzer.cookbook.services.TextractProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TextractConfig {

    @Autowired
    private TextractJobRepository jobRepository;

    @Profile({"production", "default"})
    @Bean
    public TextractProvider awsProvider(AWSProperties awsProps, AmazonTextract textractClient) {
        return new AWSTextractProvider(textractClient, jobRepository, awsProps.getBucketName());
    }

    @Profile("test")
    @Bean
    public TextractProvider noOpProvider() {
        return new TextractProvider() {
            @Override
            public void processJob(long jobId) {
                throw new UnsupportedOperationException("yeah, um, no?");
            }
        };
    }

}
