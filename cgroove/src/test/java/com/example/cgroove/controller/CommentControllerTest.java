package com.example.cgroove.controller;

import com.example.cgroove.config.FileProperties;
import com.example.cgroove.config.WebConfig;
import com.example.cgroove.dto.comment.CommentRequest;
import com.example.cgroove.dto.comment.CommentResponse;
import com.example.cgroove.security.JwtFilter;
import com.example.cgroove.security.JwtUtil;
import com.example.cgroove.security.WithCustomMockUser;
import com.example.cgroove.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CommentController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private FileProperties fileProperties;

    private CommentResponse createMockResponse() {
        return new CommentResponse(
                1L,
                "테스트 댓글입니다.",
                1L,
                "TestUser",
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true
        );
    }

    @Test
    @DisplayName("댓글 작성 성공 - 게시글")
    @WithCustomMockUser
    void createComment_Post_Success() throws Exception {
        // given
        CommentRequest request = new CommentRequest("테스트 댓글입니다.", 1L, null);
        given(commentService.createComment(any(), any(CommentRequest.class))).willReturn(createMockResponse());

        // when & then
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("댓글 작성 성공"))
                .andExpect(jsonPath("$.data.commentId").value(1L))
                .andExpect(jsonPath("$.data.content").value("테스트 댓글입니다."));
    }

    @Test
    @DisplayName("댓글 작성 성공 - 행사")
    @WithCustomMockUser
    void createComment_Event_Success() throws Exception {
        // given
        CommentRequest request = new CommentRequest("행사 댓글입니다.", null, 1L);
        given(commentService.createComment(any(), any(CommentRequest.class))).willReturn(createMockResponse());

        // when & then
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("댓글 작성 성공"));
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회 성공")
    @WithCustomMockUser
    void getComments_ByPostId_Success() throws Exception {
        // given
        given(commentService.getComments(eq(1L), eq(null), any())).willReturn(List.of(createMockResponse()));

        // when & then
        mockMvc.perform(get("/comments")
                        .param("postId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 조회 성공"))
                .andExpect(jsonPath("$.data[0].commentId").value(1L));
    }

    @Test
    @DisplayName("행사 댓글 목록 조회 성공")
    @WithCustomMockUser
    void getComments_ByEventId_Success() throws Exception {
        // given
        given(commentService.getComments(eq(null), eq(1L), any())).willReturn(List.of(createMockResponse()));

        // when & then
        mockMvc.perform(get("/comments")
                        .param("eventId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 조회 성공"))
                .andExpect(jsonPath("$.data[0].commentId").value(1L));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @WithCustomMockUser
    void updateComment_Success() throws Exception {
        // given
        CommentRequest request = new CommentRequest("수정된 댓글입니다.", null, null);
        CommentResponse updatedResponse = new CommentResponse(
                1L, "수정된 댓글입니다.", 1L, "TestUser", null,
                LocalDateTime.now(), LocalDateTime.now(), true
        );
        given(commentService.updateComment(any(), eq(1L), eq("수정된 댓글입니다."))).willReturn(updatedResponse);

        // when & then
        mockMvc.perform(patch("/comments/{commentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"))
                .andExpect(jsonPath("$.data.content").value("수정된 댓글입니다."));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @WithCustomMockUser
    void deleteComment_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/comments/{commentId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(any(), eq(1L));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용 없음")
    @WithCustomMockUser
    void createComment_Fail_EmptyContent() throws Exception {
        // given
        CommentRequest request = new CommentRequest("", 1L, null);

        // when & then
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}