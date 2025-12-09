package com.example.dance_community.dto.comment;

import com.example.dance_community.entity.Comment;
import com.example.dance_community.entity.User;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,

        // 작성자 정보
        Long userId,
        String nickname,
        String profileImage,

        // 날짜
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // 작성자 여부
        boolean isMyComment
) {
    public static CommentResponse from(Comment comment, Long currentUserId) {
        User author = comment.getUser();
        boolean isOwner = author.getUserId().equals(currentUserId);

        return new CommentResponse(
                comment.getCommentId(),
                comment.getContent(),
                author.getUserId(),
                author.getNickname(),
                author.getProfileImage(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                isOwner
        );
    }
}