package com.example.cgroove.enums;

public enum EventType {
    WORKSHOP("워크샵"),
    BATTLE("배틀"),
    JAM("잼"),
    PERFORMANCE("공연");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
