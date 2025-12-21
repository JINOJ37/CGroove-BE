package com.example.cgroove.integration;

import com.example.cgroove.entity.Event;
import com.example.cgroove.entity.User;
import com.example.cgroove.repository.EventRepository;
import com.example.cgroove.repository.UserRepository;
import com.example.cgroove.security.UserDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // ✅ 추가
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // ✅ 추가

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class EventIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private EventRepository eventRepository;
        @Autowired
        private UserRepository userRepository;

        @Test
        @DisplayName("통합 테스트: 행사 생성부터 DB 저장 확인까지")
        void createEvent_EndToEnd() throws Exception {
                // given
                User savedUser = userRepository.save(User.builder()
                                .email("test@email.com")
                                .password("password")
                                .nickname("Tester")
                                .build());

                MockMultipartFile imageFile = new MockMultipartFile(
                                "images",
                                "test.jpg",
                                "image/jpeg",
                                "dummy content".getBytes());

                UserDetail userDetail = new UserDetail(
                                savedUser.getUserId(),
                                savedUser.getEmail(),
                                savedUser.getNickname(),
                                null,
                                savedUser.getPassword());

                // when
                mockMvc.perform(multipart("/events")
                                .file(imageFile)
                                .param("title", "Integration Event")
                                .param("content", "Content")
                                .param("scope", "GLOBAL")
                                .param("type", "WORKSHOP")
                                .param("locationName", "Seoul")
                                .param("capacity", "50")
                                .param("startsAt", LocalDateTime.now().toString())
                                .param("endsAt", LocalDateTime.now().plusHours(2).toString())

                                .with(user(userDetail))
                                .with(csrf()))
                                .andExpect(status().isCreated());

                // then
                List<Event> events = eventRepository.findAll();
                assertThat(events).hasSize(1);
                assertThat(events.getFirst().getTitle()).isEqualTo("Integration Event");
                assertThat(events.getFirst().getHost().getUserId()).isEqualTo(savedUser.getUserId());
        }
}