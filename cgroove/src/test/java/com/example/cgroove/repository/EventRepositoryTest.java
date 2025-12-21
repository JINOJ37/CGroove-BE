package com.example.cgroove.repository;

import com.example.cgroove.config.JpaConfig;
import com.example.cgroove.config.QueryDslConfig;
import com.example.cgroove.entity.Club;
import com.example.cgroove.entity.Event;
import com.example.cgroove.entity.User;
import com.example.cgroove.enums.ClubType;
import com.example.cgroove.enums.EventType;
import com.example.cgroove.enums.Scope;
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
@Import({QueryDslConfig.class, JpaConfig.class})
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    private User host;
    private Club myClub;

    @BeforeEach
    void setUp() {
        host = userRepository.save(new User("host@test.com", "pw", "HostUser", null));
        myClub = clubRepository.save(Club.builder().clubName("MyClub").clubType(ClubType.CLUB).build());
    }

    @Test
    @DisplayName("QueryDSL - 다가오는 행사 조회 (날짜 필터링 & 정렬 확인)")
    void findUpcomingEvents_Success() {
        // given
        eventRepository.save(Event.builder()
                .host(host).title("Past Event").scope(Scope.GLOBAL).type(EventType.BATTLE).content("Old")
                .capacity(10L).startsAt(LocalDateTime.now().minusDays(1)).endsAt(LocalDateTime.now().plusHours(1))
                .likeCount(10L).viewCount(10L).build());
        eventRepository.save(Event.builder()
                .host(host).title("Future Event").scope(Scope.GLOBAL).type(EventType.WORKSHOP).content("New")
                .capacity(20L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());

        // when
        List<Event> results = eventRepository.findUpcomingEvents(Collections.emptyList(), PageRequest.of(0, 10));

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTitle()).isEqualTo("Future Event");
        assertThat(results.getFirst().getHost().getNickname()).isEqualTo("HostUser");
    }

    @Test
    @DisplayName("QueryDSL - 행사 목록 조회 (권한 필터링 확인)")
    void findAllEvents_ScopeCheck() {
        // given
        eventRepository.save(Event.builder()
                .host(host).title("Global Event").scope(Scope.GLOBAL).type(EventType.JAM).content("Global")
                .capacity(10L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());
        eventRepository.save(Event.builder()
                .host(host).title("Club Event").scope(Scope.CLUB).club(myClub).type(EventType.JAM).content("Club")
                .capacity(10L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());
        Club otherClub = clubRepository.save(Club.builder().clubName("OtherClub").clubType(ClubType.CREW).build());
        eventRepository.save(Event.builder()
                .host(host).title("Other Event").scope(Scope.CLUB).club(otherClub).type(EventType.JAM).content("Other")
                .capacity(10L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());

        // when
        List<Long> myClubIds = List.of(myClub.getClubId());
        List<Event> results = eventRepository.findAllEvents(myClubIds);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("title")
                .containsExactlyInAnyOrder("Global Event", "Club Event");
        assertThat(results).extracting("title")
                .doesNotContain("Other Event");
    }
}