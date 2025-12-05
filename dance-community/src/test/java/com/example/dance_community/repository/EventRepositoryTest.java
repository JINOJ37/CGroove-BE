package com.example.dance_community.repository;

import com.example.dance_community.config.JpaConfig;
import com.example.dance_community.config.QueryDslConfig;
import com.example.dance_community.entity.Club;
import com.example.dance_community.entity.Event;
import com.example.dance_community.entity.User;
import com.example.dance_community.enums.ClubType;
import com.example.dance_community.enums.EventType;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class}) // QueryDSL, Auditing 설정 로드
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EntityManager em;

    private User host;
    private Club myClub;

    @BeforeEach
    void setUp() {
        // 1. 유저 생성
        host = userRepository.save(new User("host@test.com", "pw", "HostUser", null));

        // 2. 클럽 생성
        myClub = clubRepository.save(Club.builder().clubName("MyClub").clubType(ClubType.CLUB).build());
    }

    @Test
    @DisplayName("QueryDSL - 다가오는 행사 조회 (날짜 필터링 & 정렬 확인)")
    void findUpcomingEvents_Success() {
        // given
        // 1. 과거 행사 (어제 시작 - 조회되면 안 됨)
        eventRepository.save(Event.builder()
                .host(host).title("Past Event").scope(Scope.GLOBAL).type(EventType.BATTLE).content("Old")
                .capacity(10L).startsAt(LocalDateTime.now().minusDays(1)).endsAt(LocalDateTime.now().plusHours(1))
                .likeCount(10L).viewCount(10L).build());

        // 2. 미래 행사 (내일 시작 - 조회되어야 함)
        eventRepository.save(Event.builder()
                .host(host).title("Future Event").scope(Scope.GLOBAL).type(EventType.WORKSHOP).content("New")
                .capacity(20L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());

        // when
        List<Event> results = eventRepository.findUpcomingEvents(Collections.emptyList(), PageRequest.of(0, 10));

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Future Event");
        // Fetch Join 확인: 호스트 정보 접근 시 쿼리 안 나가는지 (로딩됨)
        assertThat(results.getFirst().getHost().getNickname()).isEqualTo("HostUser");
    }

    @Test
    @DisplayName("QueryDSL - 행사 목록 조회 (권한 필터링 확인)")
    void findAllEvents_ScopeCheck() {
        // given
        // 1. 전체 공개 행사 (누구나 조회 가능)
        eventRepository.save(Event.builder()
                .host(host).title("Global Event").scope(Scope.GLOBAL).type(EventType.JAM).content("Global")
                .capacity(10L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());

        // 2. 내 클럽 행사 (내 클럽 ID 포함 시 조회 가능)
        eventRepository.save(Event.builder()
                .host(host).title("Club Event").scope(Scope.CLUB).club(myClub).type(EventType.JAM).content("Club")
                .capacity(10L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());

        // 3. 남의 클럽 행사 (조회 안 되어야 함)
        Club otherClub = clubRepository.save(Club.builder().clubName("OtherClub").clubType(ClubType.CREW).build());
        eventRepository.save(Event.builder()
                .host(host).title("Other Event").scope(Scope.CLUB).club(otherClub).type(EventType.JAM).content("Other")
                .capacity(10L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());

        // when: 내 클럽 ID만 포함해서 조회
        List<Long> myClubIds = List.of(myClub.getClubId());
        List<Event> results = eventRepository.findAllEvents(myClubIds);

        // then: Global(1) + MyClub(1) = 2개만 조회되어야 함
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title")
                .containsExactlyInAnyOrder("Global Event", "Club Event");

        assertThat(results).extracting("title")
                .doesNotContain("Other Event");
    }
}