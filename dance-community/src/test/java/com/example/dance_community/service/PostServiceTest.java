package com.example.dance_community.service;

import com.example.dance_community.dto.post.PostCreateRequest;
import com.example.dance_community.dto.post.PostResponse;
import com.example.dance_community.dto.post.PostUpdateRequest;
import com.example.dance_community.entity.Club;
import com.example.dance_community.entity.Post;
import com.example.dance_community.entity.User;
import com.example.dance_community.enums.Scope;
import com.example.dance_community.exception.AccessDeniedException;
import com.example.dance_community.exception.InvalidRequestException;
import com.example.dance_community.exception.NotFoundException;
import com.example.dance_community.repository.PostLikeRepository;
import com.example.dance_community.repository.PostRepository;
import com.example.dance_community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private ClubAuthService clubAuthService;
    @Mock
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("게시글 생성 성공 - GLOBAL 범위")
    void createPost_Success_Global() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        PostCreateRequest request = new PostCreateRequest(
                "GLOBAL", null, "Title", "Content", List.of("tag1"), List.of("img1")
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        Post savedPost = Post.builder()
                .author(user).scope(Scope.GLOBAL).title("Title").content("Content").build();
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostResponse response = postService.createPost(userId, request);

        // then
        assertThat(response.scope()).isEqualTo("GLOBAL");
        verify(clubAuthService, never()).findByClubId(any());
    }

    @Test
    @DisplayName("게시글 생성 성공 - CLUB 범위")
    void createPost_Success_Club() {
        // given
        Long userId = 1L;
        Long clubId = 10L;
        User user = User.builder().userId(userId).build();
        Club club = Club.builder().clubId(clubId).build();

        PostCreateRequest request = new PostCreateRequest(
                "CLUB", clubId, "Title", "Content", null, null
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(clubAuthService.findByClubId(clubId)).willReturn(club);

        Post savedPost = Post.builder()
                .author(user).scope(Scope.CLUB).club(club).title("Title").build();
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostResponse response = postService.createPost(userId, request);

        // then
        assertThat(response.scope()).isEqualTo("CLUB");
        assertThat(response.clubId()).isEqualTo(clubId);
    }

    @Test
    @DisplayName("게시글 생성 실패 - 잘못된 Scope 값")
    void createPost_Fail_InvalidScope() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        PostCreateRequest request = new PostCreateRequest("INVALID_SCOPE", null, "T", "C", null, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                postService.createPost(userId, request)
        );
    }

    @Test
    @DisplayName("게시글 생성 실패 - CLUB인데 clubId 누락")
    void createPost_Fail_NoClubId() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        PostCreateRequest request = new PostCreateRequest(
                "CLUB", null, "Title", "Content", null, null
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThrows(InvalidRequestException.class, () -> postService.createPost(userId, request));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 존재하지 않는 클럽 (CLUB Scope)")
    void createPost_Fail_ClubNotFound() {
        // given
        Long userId = 1L;
        Long clubId = 999L;
        User user = User.builder().userId(userId).build();
        PostCreateRequest request = new PostCreateRequest("CLUB", clubId, "T", "C", null, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(clubAuthService.findByClubId(clubId)).willThrow(new NotFoundException("클럽 없음"));

        // when & then
        assertThrows(NotFoundException.class, () ->
                postService.createPost(userId, request)
        );
    }

    @Test
    @DisplayName("게시글 생성 실패 - 존재하지 않는 사용자")
    void createPost_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        PostCreateRequest request = new PostCreateRequest("GLOBAL", null, "T", "C", null, null);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                postService.createPost(userId, request)
        );
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        User user = User.builder().userId(userId).build();
        Post post = Post.builder().postId(postId).scope(Scope.GLOBAL).author(user).title("Old").content("Old").build();
        PostUpdateRequest request = new PostUpdateRequest("New", "New", null, null, null);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.updatePost(postId, userId, request);

        // then
        assertThat(response.title()).isEqualTo("New");
        verify(fileStorageService, times(1)).processImageUpdate(any(Post.class), any(), any());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아님")
    void updatePost_Fail_NotAuthor() {
        // given
        Long userId = 1L;
        Long otherUserId = 2L;
        User otherUser = User.builder().userId(otherUserId).build();

        Post post = Post.builder().postId(100L).scope(Scope.GLOBAL).author(otherUser).build();

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when & then
        assertThrows(AccessDeniedException.class, () ->
                postService.updatePost(100L, userId, new PostUpdateRequest("T", "C", null, null, null))
        );
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
    void updatePost_Fail_NotFound() {
        // given
        Long postId = 999L;
        PostUpdateRequest request = new PostUpdateRequest("T", "C", null, null, null);

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                postService.updatePost(postId, 1L, request)
        );
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Post post = spy(Post.builder().postId(100L).author(user).build());

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when
        postService.deletePost(userId, 100L);

        // then
        verify(post, times(1)).delete();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자가 아님")
    void deletePost_Fail_NotAuthor() {
        // given
        Long userId = 1L;
        Long otherUserId = 99L;
        User otherUser = User.builder().userId(otherUserId).build();
        Post post = Post.builder().postId(100L).author(otherUser).build();

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when & then
        assertThrows(AccessDeniedException.class, () ->
                postService.deletePost(userId, 100L)
        );
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    void deletePost_Fail_NotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                postService.deletePost(1L, postId)
        );
    }

    @Test
    @DisplayName("게시글 단건 조회 성공 - 조회수 증가 확인")
    void getPost_Success() {
        // given
        Long postId = 100L;
        Long userId = 1L;
        User author = User.builder().userId(2L).nickname("Writer").build();
        Post post = Post.builder()
                .postId(postId)
                .author(author)
                .title("Title")
                .content("Content")
                .scope(Scope.GLOBAL)
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.existsByPostPostIdAndUserUserId(postId, userId)).willReturn(false);

        // when
        PostResponse response = postService.getPost(postId, userId);

        // then
        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.isLiked()).isFalse();
        verify(postRepository).updateViewCount(postId);
    }

    @Test
    @DisplayName("게시글 조회 실패 - 존재하지 않는 게시글")
    void getPost_Fail_NotFound() {
        // given
        Long postId = 999L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                postService.getPost(postId, 1L)
        );
    }

    @Test
    @DisplayName("Hot Groove 조회 성공 - 좋아요 여부 포함")
    void getHotPosts_Success() {
        // given
        Long userId = 1L;
        Long postId = 100L;
        User user = User.builder().userId(userId).build();
        Post post = Post.builder()
                .postId(postId)
                .title("Hot Post")
                .author(user)
                .scope(Scope.GLOBAL)
                .likeCount(50L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        given(postRepository.findHotPosts(pageable)).willReturn(List.of(post));
        given(postLikeRepository.findLikedPostIds(List.of(postId), userId)).willReturn(Set.of(postId));

        // when
        List<PostResponse> responses = postService.getHotPosts(userId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().postId()).isEqualTo(postId);
        assertThat(responses.getFirst().isLiked()).isTrue();
    }

    @Test
    @DisplayName("내 클럽 소식 조회 성공")
    void getMyClubPosts_Success() {
        // given
        Long userId = 1L;
        Post post = Post.builder()
                .postId(10L)
                .author(User.builder().userId(2L).build())
                .scope(Scope.CLUB)
                .build();

        given(postRepository.findMyClubPosts(eq(userId), any(Pageable.class)))
                .willReturn(List.of(post));

        // when
        List<PostResponse> responses = postService.getMyClubPosts(userId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().postId()).isEqualTo(10L);
    }
}