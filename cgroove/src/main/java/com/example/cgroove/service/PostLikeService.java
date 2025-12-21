package com.example.cgroove.service;

import com.example.cgroove.dto.like.PostLikeResponse;
import com.example.cgroove.entity.Post;
import com.example.cgroove.entity.PostLike;
import com.example.cgroove.entity.User;
import com.example.cgroove.repository.PostLikeRepository;
import com.example.cgroove.repository.PostRepository;
import com.example.cgroove.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public PostLikeResponse toggleLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        boolean isLiked;

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.decrementLikeCount();
            isLiked = false;
        } else {
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(newLike);
            post.incrementLikeCount();
            isLiked = true;
        }

        return new PostLikeResponse(isLiked, post.getLikeCount());
    }
}

