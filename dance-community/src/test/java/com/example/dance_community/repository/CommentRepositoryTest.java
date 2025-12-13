package com.example.dance_community.repository;

import com.example.dance_community.config.JpaConfig;
import com.example.dance_community.config.QueryDslConfig;
import com.example.dance_community.entity.Comment;
import com.example.dance_community.entity.Event;
import com.example.dance_community.entity.Post;
import com.example.dance_community.entity.User;
import com.example.dance_community.enums.EventType;
import com.example.dance_community.enums.Scope;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
class CommentRepositoryTest {

    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private EntityManager em;

    private User user;
    private Post post;
    private Event event;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("test@test.com", "pw", "TestUser", null));

        post = postRepository.save(Post.builder()
                .author(user)
                .title("테스트 게시글")
                .content("내용")
                .scope(Scope.GLOBAL)
                .likeCount(0L)
                .viewCount(0L)
                .build());

        event = eventRepository.save(Event.builder()
                .host(user)
                .title("테스트 행사")
                .content("행사 내용")
                .scope(Scope.GLOBAL)
                .type(EventType.WORKSHOP)
                .capacity(50L)
                .startsAt(LocalDateTime.now().plusDays(1))
                .endsAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .likeCount(0L)
                .viewCount(0L)
                .build());
    }

    @Test
    @DisplayName("게시글 ID로 댓글 조회")
    void findByPost_PostId() {
        // given
        Comment comment1 = commentRepository.save(Comment.builder()
                .user(user)
                .post(post)
                .content("첫번째 댓글")
                .build());
        Comment comment2 = commentRepository.save(Comment.builder()
                .user(user)
                .post(post)
                .content("두번째 댓글")
                .build());
        // 다른 게시글의 댓글 (조회되면 안됨)
        Post otherPost = postRepository.save(Post.builder()
                .author(user)
                .title("다른 게시글")
                .content("내용")
                .scope(Scope.GLOBAL)
                .likeCount(0L)
                .viewCount(0L)
                .build());
        commentRepository.save(Comment.builder()
                .user(user)
                .post(otherPost)
                .content("다른 게시글 댓글")
                .build());

        em.flush();
        em.clear();

        // when
        List<Comment> comments = commentRepository.findByPost_PostId(post.getPostId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting("content")
                .containsExactlyInAnyOrder("첫번째 댓글", "두번째 댓글");
    }

    @Test
    @DisplayName("행사 ID로 댓글 조회")
    void findByEvent_EventId() {
        // given
        Comment comment1 = commentRepository.save(Comment.builder()
                .user(user)
                .event(event)
                .content("행사 댓글 1")
                .build());
        Comment comment2 = commentRepository.save(Comment.builder()
                .user(user)
                .event(event)
                .content("행사 댓글 2")
                .build());

        em.flush();
        em.clear();

        // when
        List<Comment> comments = commentRepository.findByEvent_EventId(event.getEventId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting("content")
                .containsExactlyInAnyOrder("행사 댓글 1", "행사 댓글 2");
    }

    @Test
    @DisplayName("삭제된 댓글은 조회되지 않음 (Soft Delete)")
    void softDelete_NotReturned() {
        // given
        Comment activeComment = commentRepository.save(Comment.builder()
                .user(user)
                .post(post)
                .content("활성 댓글")
                .build());
        Comment deletedComment = commentRepository.save(Comment.builder()
                .user(user)
                .post(post)
                .content("삭제된 댓글")
                .build());

        em.flush();
        em.clear();

        // 삭제 처리
        Comment toDelete = commentRepository.findById(deletedComment.getCommentId()).orElseThrow();
        commentRepository.delete(toDelete);

        em.flush();
        em.clear();

        // when
        List<Comment> comments = commentRepository.findByPost_PostId(post.getPostId());

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getContent()).isEqualTo("활성 댓글");
    }

    @Test
    @DisplayName("댓글 저장 및 조회")
    void saveAndFind() {
        // given
        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content("저장 테스트 댓글")
                .build();

        // when
        Comment savedComment = commentRepository.save(comment);
        em.flush();
        em.clear();

        Comment foundComment = commentRepository.findById(savedComment.getCommentId()).orElseThrow();

        // then
        assertThat(foundComment.getContent()).isEqualTo("저장 테스트 댓글");
        assertThat(foundComment.getUser().getUserId()).isEqualTo(user.getUserId());
        assertThat(foundComment.getPost().getPostId()).isEqualTo(post.getPostId());
    }

    @Test
    @DisplayName("댓글 내용 수정")
    void updateContent() {
        // given
        Comment comment = commentRepository.save(Comment.builder()
                .user(user)
                .post(post)
                .content("원래 내용")
                .build());

        em.flush();
        em.clear();

        // when
        Comment foundComment = commentRepository.findById(comment.getCommentId()).orElseThrow();
        foundComment.updateContent("수정된 내용");

        em.flush();
        em.clear();

        // then
        Comment updatedComment = commentRepository.findById(comment.getCommentId()).orElseThrow();
        assertThat(updatedComment.getContent()).isEqualTo("수정된 내용");
    }
}
