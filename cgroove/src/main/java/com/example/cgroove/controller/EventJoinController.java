package com.example.cgroove.controller;

import com.example.cgroove.dto.ApiResponse;
import com.example.cgroove.dto.eventJoin.EventJoinResponse;
import com.example.cgroove.security.UserDetail;
import com.example.cgroove.service.EventJoinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "7_EventJoin", description = "행사 신청 관련 API")
public class EventJoinController {
    private final EventJoinService eventJoinService;

    // 일반 사용자용
    @Operation(summary = "행사 신청", description = "행사에 신청합니다.")
    @PostMapping("/{eventId}/apply")
    public ResponseEntity<ApiResponse<EventJoinResponse>> applyEvent(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId
    ) {
        EventJoinResponse response = eventJoinService.applyEvent(userDetail.getUserId(), eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("행사 신청 성공", response));
    }

    @Operation(summary = "행사 신청 취소", description = "신청했던 행사를 취소합니다.")
    @DeleteMapping("/{eventId}/apply")
    public ResponseEntity<ApiResponse<Void>> cancelEventJoin(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId
    ) {
        eventJoinService.cancelEventJoin(userDetail.getUserId(), eventId);
        return ResponseEntity.ok(new ApiResponse<>("행사 신청 취소 성공", null));
    }

    @Operation(summary = "내 신청 상태 조회", description = "특정 행사에 대한 나의 신청 상태를 조회합니다.")
    @GetMapping("/{eventId}/my-status")
    public ResponseEntity<ApiResponse<EventJoinResponse>> getMyJoinStatus(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId
    ) {
        EventJoinResponse response = eventJoinService.getJoinStatus(userDetail.getUserId(), eventId);
        return ResponseEntity.ok(new ApiResponse<>("내 신청 상태 조회 성공", response));
    }

    @Operation(summary = "내 행사 신청 목록 조회", description = "내가 신청한 행사 목록을 조회합니다.")
    @GetMapping("/my-joins")
    public ResponseEntity<ApiResponse<List<EventJoinResponse>>> getMyEvents(
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        List<EventJoinResponse> responses = eventJoinService.getUserEvents(userDetail.getUserId());
        return ResponseEntity.ok(new ApiResponse<>("내 신청 목록 조회 성공", responses));
    }

    // 주최자 및 조회용
    @Operation(summary = "행사 참여자 목록 조회", description = "해당 행사의 확정된 참여자 목록을 조회합니다.")
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<ApiResponse<List<EventJoinResponse>>> getEventParticipants(
            @PathVariable Long eventId
    ) {
        List<EventJoinResponse> responses = eventJoinService.getEventUsers(eventId);
        return ResponseEntity.ok(new ApiResponse<>("참여자 목록 조회 성공", responses));
    }

    @Operation(summary = "행사 신청 거절", description = "행사 신청을 거절합니다.")
    @PostMapping("/{eventId}/participation/{participantId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectParticipation(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId,
            @PathVariable Long participantId
    ) {
        eventJoinService.rejectParticipation(userDetail.getUserId(), eventId, participantId);
        return ResponseEntity.ok(new ApiResponse<>("행사 신청 거절 성공", null));
    }
}