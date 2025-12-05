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
        // 1. 유저 생성
        author = userRepository.save(new User("author@test.com", "pw", "Author", null));
        viewer = userRepository.save(new User("viewer@test.com", "pw", "Viewer", null));

        // 2. 클럽 생성
        myClub = clubRepository.save(Club.builder().clubName("MyClub").clubType(ClubType.CLUB).build());
        otherClub = clubRepository.save(Club.builder().clubName("OtherClub").clubType(ClubType.CREW).build());

        // 3. 가입 정보 생성 (viewer는 myClub회원, otherClub은 비회원)
        clubJoinRepository.save(ClubJoin.builder().user(viewer).club(myClub).role(ClubRole.MEMBER).status(ClubJoinStatus.ACTIVE).build());
    }

    @Test
    @DisplayName("QueryDSL - 전체 게시글 조회 (접근 권한 필터링)")
    void findAllPosts_ScopeCheck() {
        // given
        // 1. 전체 공개 글 (보여야 함)
        postRepository.save(Post.builder().author(author).title("Global Post").scope(Scope.GLOBAL).content("G")
                .likeCount(10L).viewCount(10L).build());

        // 2. 내 클럽 글 (보여야 함)
        postRepository.save(Post.builder().author(author).title("My Club Post").scope(Scope.CLUB).club(myClub).content("M")
                .likeCount(10L).viewCount(10L).build());

        // 3. 남의 클럽 글 (안 보여야 함)
        postRepository.save(Post.builder().author(author).title("Other Club Post").scope(Scope.CLUB).club(otherClub).content("O")
                .likeCount(10L).viewCount(10L).build());

        // 4. 삭제된 글 (안 보여야 함)
        Post deleted = Post.builder().author(author).title("Deleted Post").scope(Scope.GLOBAL).content("D")
                .likeCount(10L).viewCount(10L).build();
        deleted.delete(); // isDeleted = true
        postRepository.save(deleted);

        // when
        // viewer가 가입한 클럽 ID 목록
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
        // 1. 핫 게시글 (GLOBAL, 좋아요 100) -> 1등
        postRepository.save(Post.builder().author(author).title("Hot Global").scope(Scope.GLOBAL).likeCount(100L).viewCount(10L).content("C").build());

        // 2. 덜 핫한 게시글 (GLOBAL, 좋아요 10) -> 2등
        postRepository.save(Post.builder().author(author).title("Cool Global").scope(Scope.GLOBAL).likeCount(10L).viewCount(10L).content("C").build());

        // 3. 클럽 전용 글 (좋아요 많아도 GLOBAL 아니면 제외) -> 제외
        postRepository.save(Post.builder().author(author).title("Hot Club").scope(Scope.CLUB).club(myClub).likeCount(200L).viewCount(10L).content("C").build());

        // 4. 오래된 글 (15일 전) -> 제외
        Post oldPost = postRepository.save(
                Post.builder().author(author).title("Old Post").scope(Scope.GLOBAL).likeCount(300L).viewCount(10L).content("C").build()
        );
        // 주의: 테이블명('posts')과 컬럼명('created_at', 'post_id')은 실제 DB 설정에 맞춰야 합니다.
        // 보통 CamelCase -> SnakeCase 변환되므로 created_at이 맞을 겁니다.
        em.createNativeQuery("UPDATE posts SET created_at = :pastDate WHERE post_id = :id")
                .setParameter("pastDate", LocalDateTime.now().minusDays(20)) // 20일 전
                .setParameter("id", oldPost.getPostId())
                .executeUpdate();

        // ❗ 중요: DB는 바꿨지만, 자바 캐시(영속성 컨텍스트)에는 아직 '현재 시간'으로 남아있음.
        // 캐시를 비워야 DB에서 다시 조회함.
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
        // MyClub에 쓴 글
        postRepository.save(Post.builder().author(author).title("Club Post 1").scope(Scope.CLUB).club(myClub).content("C")
                .likeCount(10L).viewCount(10L).build());

        // OtherClub에 쓴 글 (가입 안함)
        postRepository.save(Post.builder().author(author).title("Other Post").scope(Scope.CLUB).club(otherClub).content("C")
                .likeCount(10L).viewCount(10L).build());

        // when
        // viewer의 ID로 조회
        List<Post> results = postRepository.findMyClubPosts(viewer.getUserId(), PageRequest.of(0, 10));

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Club Post 1");

        // Fetch Join 확인 (Club 정보)
        assertThat(results.get(0).getClub().getClubName()).isEqualTo("MyClub");
    }

    @Test
    @DisplayName("조회수 증가 (JPA @Modifying)")
    void updateViewCount() {
        // given
        Post post = postRepository.save(Post.builder().author(author).title("View Test").scope(Scope.GLOBAL).content("C")
                .likeCount(10L).viewCount(0L).build());

        // when
        postRepository.updateViewCount(post.getPostId());
        em.clear(); // 영속성 컨텍스트 비우기 (DB 다시 조회 위해)

        // then
        Post updated = postRepository.findById(post.getPostId()).orElseThrow();
        assertThat(updated.getViewCount()).isEqualTo(1L);
    }
}