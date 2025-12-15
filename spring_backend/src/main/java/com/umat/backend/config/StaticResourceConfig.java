package com.umat.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Serve files under ./uploads (project root) at /uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(3600);
        
        // Serve static files from img directory using absolute file path
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:C:/Users/arafa/umat/umat/img/")
                .setCachePeriod(3600);
    }
}
