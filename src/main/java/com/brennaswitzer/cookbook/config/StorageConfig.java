package com.brennaswitzer.cookbook.config;

import com.amazonaws.services.s3.AmazonS3;
import com.brennaswitzer.cookbook.services.LocalStorageService;
import com.brennaswitzer.cookbook.services.S3StorageService;
import com.brennaswitzer.cookbook.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfig {

    @Autowired
    private AWSProperties awsProps;

    @Profile({"production", "development"})
    @Bean
    public StorageService s3Storage(AmazonS3 s3client) {
        return new S3StorageService(s3client, awsProps.getRegion(), awsProps.getBucketName());
    }

    @Profile("test")
    @Bean
    public StorageService localStorage() {
        return new LocalStorageService();
    }
}
