package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.web.SPAController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final long MAX_AGE_SECS = 3600;

    private final ServerProperties serverProperties;

    private final List<ErrorViewResolver> errorViewResolvers;

    public WebMvcConfig(ServerProperties serverProperties,
                        ObjectProvider<ErrorViewResolver> errorViewResolvers) {
        this.serverProperties = serverProperties;
        this.errorViewResolvers = errorViewResolvers.orderedStream()
                .collect(Collectors.toList());
    }

    @Bean
    public SPAController basicErrorController(ErrorAttributes errorAttributes) {
        return new SPAController(errorAttributes, this.serverProperties.getError(),
                this.errorViewResolvers);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);

    }
}
