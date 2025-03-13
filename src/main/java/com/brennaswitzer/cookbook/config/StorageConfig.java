package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.services.storage.LocalStorageService;
import com.brennaswitzer.cookbook.services.storage.S3StorageService;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfig {

    @Profile({"production", "default"})
    @Bean
    public StorageService s3Storage(AWSProperties awsProps,
                                    S3Client client,
                                    S3Presigner presigner) {
        return new S3StorageService(client,
                                    presigner,
                                    awsProps.getBucketName());
    }

    @Profile("test")
    @Bean
    public StorageService localStorage() {
        return new LocalStorageService();
    }

}
