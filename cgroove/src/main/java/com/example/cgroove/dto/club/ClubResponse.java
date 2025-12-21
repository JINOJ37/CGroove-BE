package com.example.cgroove.dto.club;

import com.example.cgroove.entity.Club;
import com.example.cgroove.enums.ClubType;

import java.time.LocalDateTime;
import java.util.List;

public record ClubResponse (
    Long clubId,
    String clubName,
    String intro,
    String description,
    String locationName,
    ClubType clubType,
    String clubImage,
    List<String> tags,
    Long memberCount,
    LocalDateTime createdAt
){
    public static ClubResponse from(Club club) {
        return new ClubResponse(
                club.getClubId(),
                club.getClubName(),
                club.getIntro(),
                club.getDescription(),
                club.getLocationName(),
                club.getClubType(),
                club.getClubImage(),
                club.getTags(),
                (long) club.getMemberCount(),
                club.getCreatedAt()
        );
    }
}
