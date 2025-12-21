package com.example.cgroove.service;

import com.example.cgroove.dto.comment.CommentRequest;
import com.example.cgroove.dto.comment.CommentResponse;
import com.example.cgroove.entity.Comment;
import com.example.cgroove.entity.Event;
import com.example.cgroove.entity.Post;
import com.example.cgroove.entity.User;
import com.example.cgroove.enums.Scope;
import com.example.cgroove.exception.AccessDeniedException;
import com.example.cgroove.exception.InvalidRequestException;
import com.example.cgroove.exception.NotFoundException;
import com.example.cgroove.repository.CommentRepository;
import com.example.cgroove.repository.EventRepository;
import com.example.cgroove.repository.PostRepository;
import com.example.cgroove.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private EventRepository eventRepository;

    @Test
    @DisplayName("댓글 생성 성공 - 게시글")
    void createComment_Post_Success() {
        // given
        Long userId = 1L;
        Long postId = 10L;
        User user = User.builder().userId(userId).nickname("TestUser").build();
        Post post = Post.builder().postId(postId).author(user).scope(Scope.GLOBAL).build();
        CommentRequest request = new CommentRequest("테스트 댓글", postId, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        Comment savedComment = Comment.builder()
                .commentId(1L)
                .user(user)
                .post(post)
                .content("테스트 댓글")
                .build();
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponse response = commentService.createComment(userId, request);

        // then
        assertThat(response.commentId()).isEqualTo(1L);
        assertThat(response.content()).isEqualTo("테스트 댓글");
        assertThat(response.userId()).isEqualTo(userId);
        verify(postRepository).findById(postId);
        verify(eventRepository, never()).findById(any());
    }

    @Test
    @DisplayName("댓글 생성 성공 - 행사")
    void createComment_Event_Success() {
        // given
        Long userId = 1L;
        Long eventId = 20L;
        User user = User.builder().userId(userId).nickname("TestUser").build();
        Event event = Event.builder().eventId(eventId).host(user).build();
        CommentRequest request = new CommentRequest("행사 댓글", null, eventId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

        Comment savedComment = Comment.builder()
                .commentId(1L)
                .user(user)
                .event(event)
                .content("행사 댓글")
                .build();
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponse response = commentService.createComment(userId, request);

        // then
        assertThat(response.commentId()).isEqualTo(1L);
        assertThat(response.content()).isEqualTo("행사 댓글");
        verify(eventRepository).findById(eventId);
        verify(postRepository, never()).findById(any());
    }

    @Test
    @DisplayName("댓글 생성 실패 - postId와 eventId 둘 다 없음")
    void createComment_Fail_NoTarget() {
        // given
        Long userId = 1L;
        CommentRequest request = new CommentRequest("댓글", null, null);

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                commentService.createComment(userId, request)
        );
    }

    @Test
    @DisplayName("댓글 생성 실패 - postId와 eventId 둘 다 있음")
    void createComment_Fail_BothTargets() {
        // given
        Long userId = 1L;
        CommentRequest request = new CommentRequest("댓글", 1L, 1L);

        // when & then
        assertThrows(InvalidRequestException.class, () ->
                commentService.createComment(userId, request)
        );
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 사용자")
    void createComment_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        CommentRequest request = new CommentRequest("댓글", 1L, null);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                commentService.createComment(userId, request)
        );
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 게시글")
    void createComment_Fail_PostNotFound() {
        // given
        Long userId = 1L;
        Long postId = 999L;
        User user = User.builder().userId(userId).build();
        CommentRequest request = new CommentRequest("댓글", postId, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                commentService.createComment(userId, request)
        );
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 행사")
    void createComment_Fail_EventNotFound() {
        // given
        Long userId = 1L;
        Long eventId = 999L;
        User user = User.builder().userId(userId).build();
        CommentRequest request = new CommentRequest("댓글", null, eventId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(eventRepository.findById(eventId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                commentService.createComment(userId, request)
        );
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 - 게시글")
    void getComments_ByPostId_Success() {
        // given
        Long postId = 10L;
        Long currentUserId = 1L;
        User user = User.builder().userId(currentUserId).nickname("TestUser").build();
        Comment comment = Comment.builder()
                .commentId(1L)
                .user(user)
                .content("댓글 내용")
                .build();

        given(postRepository.existsById(postId)).willReturn(true);
        given(commentRepository.findByPost_PostId(postId)).willReturn(List.of(comment));

        // when
        List<CommentResponse> responses = commentService.getComments(postId, null, currentUserId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().content()).isEqualTo("댓글 내용");
        assertThat(responses.getFirst().isMyComment()).isTrue();
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 - 행사")
    void getComments_ByEventId_Success() {
        // given
        Long eventId = 20L;
        Long currentUserId = 1L;
        User user = User.builder().userId(2L).nickname("OtherUser").build();
        Comment comment = Comment.builder()
                .commentId(1L)
                .user(user)
                .content("행사 댓글")
                .build();

        given(eventRepository.existsById(eventId)).willReturn(true);
        given(commentRepository.findByEvent_EventId(eventId)).willReturn(List.of(comment));

        // when
        List<CommentResponse> responses = commentService.getComments(null, eventId, currentUserId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().isMyComment()).isFalse();
    }

    @Test
    @DisplayName("댓글 목록 조회 실패 - 존재하지 않는 게시글")
    void getComments_Fail_PostNotFound() {
        // given
        Long postId = 999L;
        given(postRepository.existsById(postId)).willReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () ->
                commentService.getComments(postId, null, 1L)
        );
    }

    @Test
    @DisplayName("댓글 목록 조회 실패 - 존재하지 않는 행사")
    void getComments_Fail_EventNotFound() {
        // given
        Long eventId = 999L;
        given(eventRepository.existsById(eventId)).willReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () ->
                commentService.getComments(null, eventId, 1L)
        );
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        Long userId = 1L;
        Long commentId = 100L;
        User user = User.builder().userId(userId).nickname("TestUser").build();
        Comment comment = Comment.builder()
                .commentId(commentId)
                .user(user)
                .content("원래 댓글")
                .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        CommentResponse response = commentService.updateComment(userId, commentId, "수정된 댓글");

        // then
        assertThat(response.content()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
    void updateComment_Fail_NotFound() {
        // given
        Long commentId = 999L;
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                commentService.updateComment(1L, commentId, "수정")
        );
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자가 아님")
    void updateComment_Fail_NotOwner() {
        // given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long commentId = 100L;
        User otherUser = User.builder().userId(otherUserId).build();
        Comment comment = Comment.builder()
                .commentId(commentId)
                .user(otherUser)
                .content("댓글")
                .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThrows(AccessDeniedException.class, () ->
                commentService.updateComment(userId, commentId, "수정")
        );
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        Long userId = 1L;
        Long commentId = 100L;
        User user = User.builder().userId(userId).build();
        Comment comment = spy(Comment.builder()
                .commentId(commentId)
                .user(user)
                .content("삭제할 댓글")
                .build());

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(userId, commentId);

        // then
        verify(comment).delete();
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    void deleteComment_Fail_NotFound() {
        // given
        Long commentId = 999L;
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                commentService.deleteComment(1L, commentId)
        );
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 작성자가 아님")
    void deleteComment_Fail_NotOwner() {
        // given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long commentId = 100L;
        User otherUser = User.builder().userId(otherUserId).build();
        Comment comment = Comment.builder()
                .commentId(commentId)
                .user(otherUser)
                .content("댓글")
                .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThrows(AccessDeniedException.class, () ->
                commentService.deleteComment(userId, commentId)
        );
    }
}
