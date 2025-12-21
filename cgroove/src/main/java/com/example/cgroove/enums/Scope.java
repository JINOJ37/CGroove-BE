package com.example.cgroove.enums;

public enum Scope {
    GLOBAL("전체 공개"),
    CLUB("클럽 공개");

    private final String description;

    Scope(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}