package com.example.cgroove.service;

import com.example.cgroove.dto.like.PostLikeResponse;
import com.example.cgroove.entity.Post;
import com.example.cgroove.entity.PostLike;
import com.example.cgroove.entity.User;
import com.example.cgroove.repository.PostLikeRepository;
import com.example.cgroove.repository.PostRepository;
import com.example.cgroove.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @InjectMocks
    private PostLikeService postLikeService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("좋아요 추가 성공")
    void toggleLike_Add() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        User user = User.builder().userId(userId).build();
        Post post = Post.builder().postId(postId).likeCount(0L).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.empty());

        // when
        PostLikeResponse response = postLikeService.toggleLike(userId, postId);

        // then
        assertThat(response.isLiked()).isTrue();
        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        assertThat(response.likeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void toggleLike_Remove() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        User user = User.builder().userId(userId).build();
        Post post = Post.builder().postId(postId).likeCount(1L).build();
        PostLike existingLike = PostLike.builder().post(post).user(user).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.of(existingLike));

        // when
        PostLikeResponse response = postLikeService.toggleLike(userId, postId);

        // then
        assertThat(response.isLiked()).isFalse();
        verify(postLikeRepository, times(1)).delete(existingLike);
        assertThat(response.likeCount()).isEqualTo(0L);
    }
}