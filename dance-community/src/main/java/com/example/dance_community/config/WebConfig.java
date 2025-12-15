package com.example.dance_community.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final FileProperties fileProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 정상 경로
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/home/ec2-user/app/uploads/");

        // 2. 꼬인 경로 (👇 이게 꼭 있어야 합니다!)
        registry.addResourceHandler("/home/ec2-user/app/uploads/**")
                .addResourceLocations("file:/home/ec2-user/app/uploads/");
    }
}