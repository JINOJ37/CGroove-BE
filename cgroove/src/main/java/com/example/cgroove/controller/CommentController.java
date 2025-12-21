package com.example.cgroove.controller;

import com.example.cgroove.dto.ApiResponse;
import com.example.cgroove.dto.comment.CommentRequest;
import com.example.cgroove.dto.comment.CommentResponse;
import com.example.cgroove.security.UserDetail;
import com.example.cgroove.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "8_Comment", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "게시글 또는 행사에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse response = commentService.createComment(userDetail.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("댓글 작성 성공", response));
    }

    @Operation(summary = "댓글 조회", description = "게시글 또는 행사의 댓글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @AuthenticationPrincipal UserDetail userDetail,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long eventId
    ) {
        List<CommentResponse> comments = commentService.getComments(
                postId,
                eventId,
                userDetail.getUserId()
        );
        return ResponseEntity.ok(new ApiResponse<>("댓글 조회 성공", comments));
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글의 내용을 수정합니다.")
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse response = commentService.updateComment(
                userDetail.getUserId(),
                commentId,
                request.getContent()
        );
        return ResponseEntity.ok(new ApiResponse<>("댓글 수정 성공", response));
    }

    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userDetail.getUserId(), commentId);
        return ResponseEntity.noContent().build();
    }
}
