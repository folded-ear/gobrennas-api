package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.services.LocalStorageService;
import com.brennaswitzer.cookbook.services.S3StorageService;
import com.brennaswitzer.cookbook.services.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfig {

    @Profile({"production", "default"})
    @Bean
    public StorageService s3Storage(AWSProperties awsProps, S3Client s3client) {
        return new S3StorageService(s3client, awsProps.getRegion(), awsProps.getBucketName());
    }

    @Profile("test")
    @Bean
    public StorageService localStorage() {
        return new LocalStorageService();
    }

}
