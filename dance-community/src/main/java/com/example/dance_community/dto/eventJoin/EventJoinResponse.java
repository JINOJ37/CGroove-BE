package com.example.dance_community.dto.eventJoin;

import com.example.dance_community.entity.EventJoin;

import java.time.LocalDateTime;

public record EventJoinResponse(
        Long eventJoinId,
        Long userId,
        String nickname,
        String email,
        String profileImage,
        Long eventId,
        String status,
        LocalDateTime createdAt
) {
    public static EventJoinResponse from(EventJoin eventJoin) {
        return new EventJoinResponse(
                eventJoin.getEventJoinId(),
                eventJoin.getParticipant().getUserId(),
                eventJoin.getParticipant().getNickname(),
                eventJoin.getParticipant().getEmail(),
                eventJoin.getParticipant().getProfileImage(),
                eventJoin.getEvent().getEventId(),
                eventJoin.getStatus().name(),
                eventJoin.getCreatedAt()
        );
    }
}
