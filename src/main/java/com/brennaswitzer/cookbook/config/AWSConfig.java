package com.brennaswitzer.cookbook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
@Profile({"production", "default"})
public class AWSConfig {

    @Autowired
    private AWSProperties props;

    @Bean
    public AwsCredentials awsCredentials() {
        return AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());
    }

    @Bean
    public S3Client s3client() {
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials()))
                .region(Region.of(props.getRegion()))
                .build();
    }

    @Bean
    public TextractClient textractClient() {
        return TextractClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials()))
                .region(Region.of(props.getRegion()))
                .build();
    }

}
