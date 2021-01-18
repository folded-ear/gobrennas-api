package com.brennaswitzer.cookbook;

import com.brennaswitzer.cookbook.config.AWSProperties;
import com.brennaswitzer.cookbook.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, AWSProperties.class})
public class CookbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookbookApplication.class, args);
    }

}
