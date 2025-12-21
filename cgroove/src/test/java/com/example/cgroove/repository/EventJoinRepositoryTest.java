package com.example.cgroove.repository;

import com.example.cgroove.config.JpaConfig;
import com.example.cgroove.config.QueryDslConfig;
import com.example.cgroove.entity.Event;
import com.example.cgroove.entity.EventJoin;
import com.example.cgroove.entity.User;
import com.example.cgroove.enums.EventJoinStatus;
import com.example.cgroove.enums.EventType;
import com.example.cgroove.enums.Scope;
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
class EventJoinRepositoryTest {

    @Autowired
    private EventJoinRepository eventJoinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User participant;
    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        User host = userRepository.save(new User("host@test.com", "pw", "HostUser", null));
        participant = userRepository.save(new User("parti@test.com", "pw", "Participant", null));

        event1 = eventRepository.save(Event.builder()
                .host(host).title("Event A").scope(Scope.GLOBAL).type(EventType.WORKSHOP).content("Content A")
                .capacity(20L).startsAt(LocalDateTime.now().plusDays(1)).endsAt(LocalDateTime.now().plusDays(2))
                .likeCount(10L).viewCount(10L).build());
        event2 = eventRepository.save(Event.builder()
                .host(host).title("Event B").scope(Scope.GLOBAL).type(EventType.BATTLE).content("Content B")
                .capacity(20L).startsAt(LocalDateTime.now().plusDays(2)).endsAt(LocalDateTime.now().plusDays(3))
                .likeCount(10L).viewCount(10L).build());
    }

    @Test
    @DisplayName("QueryDSL - 행사 참여자 목록 조회 (User Fetch Join 확인)")
    void findParticipantsWithUser_Success() {
        // given
        eventJoinRepository.save(EventJoin.builder()
                .event(event1).participant(participant).status(EventJoinStatus.CONFIRMED).build());

        User other = userRepository.save(new User("other@test.com", "pw", "Other", null));
        eventJoinRepository.save(EventJoin.builder()
                .event(event1).participant(other).status(EventJoinStatus.CONFIRMED).build());

        // when
        List<EventJoin> results = eventJoinRepository.findParticipantsWithUser(event1.getEventId(), EventJoinStatus.CONFIRMED);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.getFirst().getParticipant().getNickname()).isEqualTo("Participant");
    }

    @Test
    @DisplayName("QueryDSL - 내가 참여한 행사 목록 조회 (Event & Host Fetch Join 확인)")
    void findMyJoinedEvents_Success() {
        // given
        eventJoinRepository.save(EventJoin.builder()
                .event(event1).participant(participant).status(EventJoinStatus.CONFIRMED).build());

        eventJoinRepository.save(EventJoin.builder()
                .event(event2).participant(participant).status(EventJoinStatus.CONFIRMED).build());

        // when
        List<EventJoin> results = eventJoinRepository.findMyJoinedEvents(participant.getUserId(), EventJoinStatus.CONFIRMED);

        // then
        assertThat(results).hasSize(2);
        EventJoin firstJoin = results.getFirst();
        assertThat(firstJoin.getEvent()).isNotNull();
        assertThat(firstJoin.getEvent().getHost().getNickname()).isEqualTo("HostUser");
    }

    @Test
    @DisplayName("참여 인원 수 카운트 (JPA 기본 메서드 검증)")
    void countByEvent_EventIdAndStatus() {
        // given
        eventJoinRepository.save(EventJoin.builder()
                .event(event1).participant(participant).status(EventJoinStatus.CONFIRMED).build());

        User other = userRepository.save(new User("other2@test.com", "pw", "Other2", null));
        eventJoinRepository.save(EventJoin.builder()
                .event(event1).participant(other).status(EventJoinStatus.CONFIRMED).build());

        // when
        long count = eventJoinRepository.countByEvent_EventIdAndStatus(event1.getEventId(), EventJoinStatus.CONFIRMED);

        // then
        assertThat(count).isEqualTo(2);
    }
}