package com.example.cgroove.enums;

public enum ClubType {
    CLUB("동아리"),
    CREW("크루");

    private final String description;

    ClubType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}