package com.example.cgroove.enums;

public enum ClubRole {
    LEADER("클럽장"),
    MANAGER("운영진"),
    MEMBER("멤버");

    private final String description;

    ClubRole(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}