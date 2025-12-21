package com.example.cgroove.enums;

public enum ClubJoinStatus {
    PENDING("대기중"),
    CANCELED("취소"),
    REJECTED("거절됨"),
    ACTIVE("활동중"),
    LEFT("탈퇴됨");

    private final String description;

    ClubJoinStatus(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}