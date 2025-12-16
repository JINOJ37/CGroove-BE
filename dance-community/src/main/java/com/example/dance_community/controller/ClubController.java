package com.example.dance_community.controller;

import com.example.dance_community.dto.ApiResponse;
import com.example.dance_community.dto.club.ClubCreateRequest;
import com.example.dance_community.dto.club.ClubResponse;
import com.example.dance_community.dto.club.ClubUpdateRequest;
import com.example.dance_community.security.UserDetail;
import com.example.dance_community.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Tag(name = "3_Club", description = "클럽 관련 API")
public class ClubController {
    private final ClubService clubService;

    @Operation(summary = "클럽 생성", description = "클럽을 새로 작성합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ClubResponse>> createClub(
            @AuthenticationPrincipal UserDetail userDetail,
            @ModelAttribute ClubCreateRequest request
    ) {
        ClubResponse clubResponse = clubService.createClub(userDetail.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("클럽 생성 성공", clubResponse));
    }

    @Operation(summary = "클럽 조회", description = "클럽 id를 통해 정보를 불러옵니다.")
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse<ClubResponse>> getClub(
            @PathVariable Long clubId
    ) {
        ClubResponse clubResponse = clubService.getClub(clubId);
        return ResponseEntity.ok(new ApiResponse<>("클럽 조회 성공", clubResponse));
    }

    @Operation(summary = "전체 클럽 조회", description = "전체 클럽의 정보를 불러옵니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubResponse>>> getClubs(
    ) {
        List<ClubResponse> clubResponses = clubService.getClubs();
        return ResponseEntity.ok(new ApiResponse<>("클럽 전체 조회 성공", clubResponses));
    }

    @Operation(summary = "내 클럽 수정", description = "사용자의 클럽을 수정합니다.")
    @PatchMapping(value = "/{clubId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ClubResponse>> updateClub(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long clubId,
            @ModelAttribute ClubUpdateRequest request
    ) {
        ClubResponse clubResponse = clubService.updateClub(userDetail.getUserId(), clubId, request);
        return ResponseEntity.ok(new ApiResponse<>("클럽 수정 성공", clubResponse));
    }

    @Operation(summary = "클럽 이미지 삭제", description = "클럽 이미지를 삭제합니다.")
    @DeleteMapping("/{clubId}/club-image")
    public ResponseEntity<ApiResponse<ClubResponse>> deleteClubImage(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long clubId
    ) {
        ClubResponse clubResponse = clubService.deleteClubImage(userDetail.getUserId(), clubId);
        return ResponseEntity.ok(new ApiResponse<>("클럽 이미지 삭제 성공", clubResponse));
    }


    @Operation(summary = "클럽 삭제", description = "클럽 id를 통해 정보를 삭제합니다.")
    @DeleteMapping("/{clubId}")
    public ResponseEntity<Void> deleteClub(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long clubId
    ) {
        clubService.deleteClub(userDetail.getUserId(), clubId);
        return ResponseEntity.noContent().build();
    }
}
