package com.example.dance_community.dto.post;

import com.example.dance_community.entity.Post;
import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long postId,
        Long authorId,
        String authorNickname,
        String authorProfileImage,
        String scope,
        Long clubId,
        String clubName,
        String title,
        String content,
        List<String> tags,
        List<String> images,
        Long viewCount,
        Long likeCount,
        Boolean isLiked,
        Integer commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post, Boolean isLiked) {
        return new PostResponse(
                post.getPostId(),
                post.getAuthor().getUserId(),
                post.getAuthor().getNickname(),
                post.getAuthor().getProfileImage(),
                post.getScope().name(),
                post.getClub() != null ? post.getClub().getClubId() : null,
                post.getClub() != null ? post.getClub().getClubName() : null,
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getImages(),
                post.getViewCount(),
                post.getLikeCount(),
                isLiked,
                post.getComments().size(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}