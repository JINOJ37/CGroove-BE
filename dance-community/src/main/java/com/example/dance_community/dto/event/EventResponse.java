package com.example.dance_community.dto.event;

import com.example.dance_community.entity.Event;

public record EventResponse(
        Long eventId,
        Long hostId,
        String hostNickname,
        String hostProfileImage,
        String scope,
        Long clubId,
        String clubName,
        String type,
        String title,
        String content,
        java.util.List<String> tags,
        java.util.List<String> images,
        String locationName,
        String locationAddress,
        String locationLink,
        Long capacity,
        java.time.LocalDateTime startsAt,
        java.time.LocalDateTime endsAt,
        Long viewCount,
        Long likeCount,
        Boolean isLiked,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
) {
    public static EventResponse from(Event event, Boolean isLiked) {
        return new EventResponse(
                event.getEventId(),
                event.getHost().getUserId(),
                event.getHost().getNickname(),
                event.getHost().getProfileImage(),
                event.getScope().name(),
                event.getClub() != null ? event.getClub().getClubId() : null,
                event.getClub() != null ? event.getClub().getClubName() : null,
                event.getType().name(),
                event.getTitle(),
                event.getContent(),
                event.getTags(),
                event.getImages(),
                event.getLocationName(),
                event.getLocationAddress(),
                event.getLocationLink(),
                event.getCapacity(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getViewCount(),
                event.getLikeCount(),
                isLiked,
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}