package com.example.dance_community.dto.like;

import com.example.dance_community.entity.PostLike;

public record PostLikeResponse(
        Boolean isLiked,
        Long likeCount
) {
    public static PostLikeResponse from(PostLike postLike, Long likeCount) {
        return new PostLikeResponse(
                postLike != null,
                likeCount
        );
    }
}
