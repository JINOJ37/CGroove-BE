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
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///home/ec2-user/app/uploads/")
                .setCachePeriod(60 * 60 * 24 * 365) // 1년 캐시 설정 예시
                .resourceChain(true); // 리소스 체인 활성화
    }
}