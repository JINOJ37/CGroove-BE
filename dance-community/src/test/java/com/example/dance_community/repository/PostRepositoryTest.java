package com.example.dance_community.repository;

import com.example.dance_community.config.JpaConfig;
import com.example.dance_community.config.QueryDslConfig;
import com.example.dance_community.entity.Club;
import com.example.dance_community.entity.ClubJoin;
import com.example.dance_community.entity.Post;
import com.example.dance_community.entity.User;
import com.example.dance_community.enums.ClubJoinStatus;
import com.example.dance_community.enums.ClubRole;
import com.example.dance_community.enums.ClubType;
import com.example.dance_community.enums.Scope;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
class PostRepositoryTest {

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private ClubJoinRepository clubJoinRepository;
    @Autowired private EntityManager em;

    private User author;
    private User viewer;
    private Club myClub;
    private Club otherClub;

    @BeforeEach
    void setUp() {
        author = userRepository.save(new User("author@test.com", "pw", "Author", null));
        viewer = userRepository.save(new User("viewer@test.com", "pw", "Viewer", null));

        myClub = clubRepository.save(Club.builder().clubName("MyClub").clubType(ClubType.CLUB).build());
        otherClub = clubRepository.save(Club.builder().clubName("OtherClub").clubType(ClubType.CREW).build());

        clubJoinRepository.save(ClubJoin.builder().user(viewer).club(myClub).role(ClubRole.MEMBER).status(ClubJoinStatus.ACTIVE).build());
    }

    @Test
    @DisplayName("QueryDSL - 전체 게시글 조회 (접근 권한 필터링)")
    void findAllPosts_ScopeCheck() {
        // given
        postRepository.save(Post.builder().author(author).title("Global Post").scope(Scope.GLOBAL).content("G")
                .likeCount(10L).viewCount(10L).build());
        postRepository.save(Post.builder().author(author).title("My Club Post").scope(Scope.CLUB).club(myClub).content("M")
                .likeCount(10L).viewCount(10L).build());
        postRepository.save(Post.builder().author(author).title("Other Club Post").scope(Scope.CLUB).club(otherClub).content("O")
                .likeCount(10L).viewCount(10L).build());
        Post deleted = Post.builder().author(author).title("Deleted Post").scope(Scope.GLOBAL).content("D")
                .likeCount(10L).viewCount(10L).build();
        deleted.delete();
        postRepository.save(deleted);

        // when
        List<Long> myClubIds = List.of(myClub.getClubId());
        List<Post> results = postRepository.findAllPosts(myClubIds);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title")
                .containsExactlyInAnyOrder("Global Post", "My Club Post");
    }

    @Test
    @DisplayName("QueryDSL - 핫 게시글 조회 (기간, 좋아요 순, GLOBAL 체크)")
    void findHotPosts_FilterAndSort() {
        // given
        postRepository.save(Post.builder().author(author).title("Hot Global").scope(Scope.GLOBAL).likeCount(100L).viewCount(10L).content("C").build());
        postRepository.save(Post.builder().author(author).title("Cool Global").scope(Scope.GLOBAL).likeCount(10L).viewCount(10L).content("C").build());
        postRepository.save(Post.builder().author(author).title("Hot Club").scope(Scope.CLUB).club(myClub).likeCount(200L).viewCount(10L).content("C").build());
        Post oldPost = postRepository.save(
                Post.builder().author(author).title("Old Post").scope(Scope.GLOBAL).likeCount(300L).viewCount(10L).content("C").build()
        );
        em.createNativeQuery("UPDATE posts SET created_at = :pastDate WHERE post_id = :id")
                .setParameter("pastDate", LocalDateTime.now().minusDays(20)) // 20일 전
                .setParameter("id", oldPost.getPostId())
                .executeUpdate();
        em.clear();

        // when
        List<Post> results = postRepository.findHotPosts(PageRequest.of(0, 10));

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("Hot Global");
        assertThat(results.get(1).getTitle()).isEqualTo("Cool Global");
    }

    @Test
    @DisplayName("QueryDSL - 내 동아리 소식 조회 (ACTIVE 멤버만, Fetch Join)")
    void findMyClubPosts_Success() {
        // given
        postRepository.save(Post.builder().author(author).title("Club Post 1").scope(Scope.CLUB).club(myClub).content("C")
                .likeCount(10L).viewCount(10L).build());
        postRepository.save(Post.builder().author(author).title("Other Post").scope(Scope.CLUB).club(otherClub).content("C")
                .likeCount(10L).viewCount(10L).build());

        // when
        List<Post> results = postRepository.findMyClubPosts(viewer.getUserId(), PageRequest.of(0, 10));

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Club Post 1");
        assertThat(results.getFirst().getClub().getClubName()).isEqualTo("MyClub");
    }

    @Test
    @DisplayName("조회수 증가 (JPA @Modifying)")
    void updateViewCount() {
        // given
        Post post = postRepository.save(Post.builder().author(author).title("View Test").scope(Scope.GLOBAL).content("C")
                .likeCount(10L).viewCount(0L).build());

        // when
        postRepository.updateViewCount(post.getPostId());
        em.clear();

        // then
        Post updated = postRepository.findById(post.getPostId()).orElseThrow();
        assertThat(updated.getViewCount()).isEqualTo(1L);
    }
}