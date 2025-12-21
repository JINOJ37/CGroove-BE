package com.example.cgroove.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageType {
    PROFILE("users", true),
    CLUB("clubs", true),
    POST("posts", false),
    EVENT("events", false);

    private final String directory;
    private final boolean allowDefault;

    public String getDirectory() {
        return directory;
    }

    public boolean allowDefault() {
        return allowDefault;
    }

    public String getTypeName() {
        return name().toLowerCase();
    }
}