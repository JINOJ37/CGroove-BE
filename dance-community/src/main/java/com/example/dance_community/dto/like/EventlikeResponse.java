package com.example.dance_community.dto.like;

public record EventlikeResponse (
        Boolean isLiked,
        Long likeCount
) {
    public static EventlikeResponse from(Boolean isLiked, Long likeCount) {
        return new EventlikeResponse(
                isLiked,
                likeCount
        );
    }
}