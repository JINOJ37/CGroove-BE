package com.example.cgroove.dto.like;

import com.example.cgroove.entity.PostLike;

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
