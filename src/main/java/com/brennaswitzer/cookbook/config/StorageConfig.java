package com.brennaswitzer.cookbook.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.brennaswitzer.cookbook.services.LocalStorageService;
import com.brennaswitzer.cookbook.services.S3StorageService;
import com.brennaswitzer.cookbook.services.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfig {

    @Value("${app.aws-access-key}")
    String accessKey;

    @Value("${app.aws-secret-key}")
    String secretKey;

    @Profile({"production", "development"})
    @Bean
    public StorageService s3Storage() {

        AWSCredentials credentials = new BasicAWSCredentials(
                accessKey,
                secretKey
        );

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_WEST_2)
                .build();
        return new S3StorageService(s3client);
    }

    @Profile("test")
    @Bean
    public StorageService localStorage() {
        return new LocalStorageService();
    }
}
