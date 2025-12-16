package com.example.dance_community.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileProperties {
    private String baseDir = "uploads";
    private String userDir = "users";
    private String clubDir = "clubs";
    private String postDir = "posts";
    private String eventDir = "events";

    public String getUploadDir() {
        return Paths.get(baseDir).toAbsolutePath().toString();
    }
}