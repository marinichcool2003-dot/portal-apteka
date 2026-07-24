package com.apteka.portal.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.default.avatars.upload.dir}")
    private String uploadAvatarsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePathToAvatars = Paths.get(uploadAvatarsDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(absolutePathToAvatars);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(splitAllowedOrigins())
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("X-XSRF-TOKEN", "Content-Type", "Authorization")
                        .allowCredentials(true);
            }
        };
    }

    private String[] splitAllowedOrigins() {
        return java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }
}
