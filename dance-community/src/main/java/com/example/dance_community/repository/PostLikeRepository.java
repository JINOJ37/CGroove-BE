package com.example.dance_community.repository;

import com.example.dance_community.entity.Post;
import com.example.dance_community.entity.PostLike;
import com.example.dance_community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostPostIdAndUserUserId(Long postId, Long userId);
}