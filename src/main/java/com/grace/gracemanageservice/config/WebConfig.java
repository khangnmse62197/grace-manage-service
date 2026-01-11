package com.grace.gracemanageservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200")  // Specify frontend origin
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")  // Allow Authorization header
            .exposedHeaders("Authorization")  // Expose Authorization header to frontend
            // NOTE: allowCredentials NOT needed for Bearer token auth (only for cookies)
            .maxAge(3600);
    }
}

