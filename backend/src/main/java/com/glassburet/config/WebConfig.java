package com.glassburet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // Ensure uploads directory exists
            File uploadsDir = new File("uploads");
            if (!uploadsDir.exists()) {
                Files.createDirectories(Paths.get("uploads"));
            }
            
            String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(uploadPath)
                    .setCachePeriod(3600);
        } catch (Exception e) {
            System.err.println("Failed to configure uploads directory: " + e.getMessage());
        }
    }
}
