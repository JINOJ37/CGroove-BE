package com.example.dance_community.repository;

import com.example.dance_community.config.JpaConfig;
import com.example.dance_community.config.QueryDslConfig;
import com.example.dance_community.entity.Club;
import com.example.dance_community.entity.ClubJoin;
import com.example.dance_community.entity.User;
import com.example.dance_community.enums.ClubJoinStatus;
import com.example.dance_community.enums.ClubRole;
import com.example.dance_community.enums.ClubType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
class ClubJoinRepositoryTest {

    @Autowired
    private ClubJoinRepository clubJoinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    private User user1;
    private User user2;
    private Club club1;
    private Club club2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(new User("user1@test.com", "pw", "User1", null));
        user2 = userRepository.save(new User("user2@test.com", "pw", "User2", null));

        club1 = clubRepository.save(Club.builder().clubName("Club A").clubType(ClubType.CLUB).build());
        club2 = clubRepository.save(Club.builder().clubName("Club B").clubType(ClubType.CREW).build());
    }

    @Test
    @DisplayName("QueryDSL - 내 클럽 목록 조회 (Club 정보 Fetch Join 확인)")
    void findMyClubJoins_Success() {
        // given
        clubJoinRepository.save(ClubJoin.builder()
                .user(user1).club(club1).role(ClubRole.MEMBER).status(ClubJoinStatus.ACTIVE).build());

        clubJoinRepository.save(ClubJoin.builder()
                .user(user1).club(club2).role(ClubRole.MEMBER).status(ClubJoinStatus.PENDING).build());

        // when
        List<ClubJoin> results = clubJoinRepository.findMyClubJoins(user1.getUserId(), List.of(ClubJoinStatus.ACTIVE));

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getClub().getClubName()).isEqualTo("Club A");
        assertThat(results.getFirst().getClub().getClubType()).isEqualTo(ClubType.CLUB);
    }

    @Test
    @DisplayName("QueryDSL - 클럽 멤버 목록 조회 (User 정보 Fetch Join 확인)")
    void findClubMembers_Success() {
        // given
        clubJoinRepository.save(ClubJoin.builder()
                .user(user1).club(club1).role(ClubRole.LEADER).status(ClubJoinStatus.ACTIVE).build());

        clubJoinRepository.save(ClubJoin.builder()
                .user(user2).club(club1).role(ClubRole.MEMBER).status(ClubJoinStatus.ACTIVE).build());

        // when
        List<ClubJoin> members = clubJoinRepository.findClubMembers(club1.getClubId(), ClubJoinStatus.ACTIVE);

        // then
        assertThat(members).hasSize(2);
        assertThat(members.get(0).getUser().getNickname()).isEqualTo("User1");
        assertThat(members.get(0).getRole()).isEqualTo(ClubRole.LEADER);
        assertThat(members.get(1).getUser().getNickname()).isEqualTo("User2");
    }
}