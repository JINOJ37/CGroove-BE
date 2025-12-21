package com.example.cgroove.controller;

import com.example.cgroove.config.FileProperties;
import com.example.cgroove.config.WebConfig;
import com.example.cgroove.dto.club.ClubJoinResponse;
import com.example.cgroove.security.JwtFilter;
import com.example.cgroove.security.JwtUtil;
import com.example.cgroove.security.WithCustomMockUser;
import com.example.cgroove.service.ClubJoinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ClubJoinController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ClubJoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubJoinService clubJoinService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private FileProperties fileProperties;

    private ClubJoinResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new ClubJoinResponse(
                1L, 1L, "User", "email", "img.jpg",
                10L, "Club", "MEMBER", "PENDING", LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("클럽 가입 신청 성공")
    @WithCustomMockUser
    void applyToClub_Success() throws Exception {
        given(clubJoinService.applyToClub(any(), eq(10L))).willReturn(mockResponse);

        mockMvc.perform(post("/clubs/{clubId}/apply", 10L)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("가입 신청 취소 성공")
    @WithCustomMockUser
    void cancelApplication_Success() throws Exception {
        mockMvc.perform(delete("/clubs/{clubId}/apply", 10L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("클럽 가입 신청 취소 성공"));
    }

    @Test
    @DisplayName("내 가입 상태 조회 성공")
    @WithCustomMockUser
    void getMyJoinStatus_Success() throws Exception {
        given(clubJoinService.getJoinStatus(any(), eq(10L))).willReturn(mockResponse);

        mockMvc.perform(get("/clubs/{clubId}/my-status", 10L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가입 신청 승인 성공")
    @WithCustomMockUser
    void approveApplication_Success() throws Exception {
        mockMvc.perform(post("/clubs/{clubId}/applications/{applicantId}/approve", 10L, 2L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가입 신청 승인 성공"));
    }

    @Test
    @DisplayName("멤버 역할 변경 성공")
    @WithCustomMockUser
    void changeMemberRole_Success() throws Exception {
        mockMvc.perform(patch("/clubs/{clubId}/members/{memberId}/role", 10L, 2L)
                        .param("newRole", "MANAGER")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("멤버 역할 변경 성공"));
    }

    @Test
    @DisplayName("멤버 추방 성공")
    @WithCustomMockUser
    void kickMember_Success() throws Exception {
        mockMvc.perform(delete("/clubs/{clubId}/members/{memberId}", 10L, 2L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("멤버 추방 성공"));
    }

    @Test
    @DisplayName("클럽 탈퇴 성공")
    @WithCustomMockUser
    void leaveClub_Success() throws Exception {
        mockMvc.perform(delete("/clubs/{clubId}/leave", 10L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("클럽 탈퇴 성공"));
    }

    @Test
    @DisplayName("내 클럽 목록 조회 성공")
    @WithCustomMockUser
    void getMyClubs_Success() throws Exception {
        given(clubJoinService.getMyClubs(any())).willReturn(java.util.List.of(mockResponse));

        mockMvc.perform(get("/clubs/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 클럽 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].clubId").value(10L));
    }

    @Test
    @DisplayName("내 클럽 목록 조회 성공 - 신청 중 포함")
    @WithCustomMockUser
    void getMyAllClubs_Success() throws Exception {
        given(clubJoinService.getMyAllClubs(any())).willReturn(java.util.List.of(mockResponse));

        mockMvc.perform(get("/clubs/my-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 클럽 목록 조회 성공"));
    }

    @Test
    @DisplayName("대기 중인 신청 목록 조회 성공")
    @WithCustomMockUser
    void getPendingApplications_Success() throws Exception {
        given(clubJoinService.getPendingApplications(any(), eq(10L))).willReturn(java.util.List.of(mockResponse));

        mockMvc.perform(get("/clubs/{clubId}/applications", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("대기 중인 신청 목록 조회 성공"));
    }

    @Test
    @DisplayName("가입 신청 거절 성공")
    @WithCustomMockUser
    void rejectApplication_Success() throws Exception {
        mockMvc.perform(post("/clubs/{clubId}/applications/{applicantId}/reject", 10L, 2L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가입 신청 거절 성공"));
    }

    @Test
    @DisplayName("활동 중인 멤버 목록 조회 성공")
    @WithCustomMockUser
    void getActiveMembers_Success() throws Exception {
        given(clubJoinService.getActiveMembers(eq(10L))).willReturn(java.util.List.of(mockResponse));

        mockMvc.perform(get("/clubs/{clubId}/members", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회 성공"));
    }
}