package com.brennaswitzer.cookbook.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"production", "default"})
public class AWSConfig {

    @Autowired
    private AWSProperties props;

    @Bean
    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(
                props.getAccessKey(),
                props.getSecretKey()
        );
    }

    @Bean
    public AmazonS3 s3client() {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                .withRegion(props.getRegion())
                .build();
    }

    @Bean
    public AmazonTextract textractClient() {
        return AmazonTextractClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                .withRegion(props.getRegion())
                .build();
    }

}
