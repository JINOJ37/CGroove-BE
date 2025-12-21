package com.example.cgroove.controller;

import com.example.cgroove.dto.ApiResponse;
import com.example.cgroove.dto.user.PasswordUpdateRequest;
import com.example.cgroove.dto.user.UserResponse;
import com.example.cgroove.dto.user.UserUpdateRequest;
import com.example.cgroove.security.UserDetail;
import com.example.cgroove.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "2_User", description = "회원 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "사용자의 정보를 불러옵니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserDetail userDetail) {
        UserResponse userResponse = userService.getUser(userDetail.getUserId());
        return ResponseEntity.ok(new ApiResponse<>("내 정보 조회 성공", userResponse));
    }

    @Operation(summary = "회원 정보 조회", description = "회원 id를 통해 정보를 불러옵니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long userId) {
        UserResponse userResponse = userService.getUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("회원 정보 조회 성공", userResponse));
    }

    @Operation(summary = "내 프로필 수정", description = "사용자 정보(닉네임, 프로필 이미지)를 수정합니다.")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @AuthenticationPrincipal UserDetail userDetail,
            @RequestPart("request") @Valid UserUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        UserResponse userResponse = userService.updateUser(userDetail.getUserId(), request, profileImage);
        return ResponseEntity.ok(new ApiResponse<>("회원 정보 수정 성공", userResponse));
    }

    @Operation(summary = "내 비밀번호 수정", description = "사용자 비밀번호를 수정합니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<UserResponse>> updatePassword(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody PasswordUpdateRequest request) {
        UserResponse userResponse = userService.updatePassword(userDetail.getUserId(), request);
        return ResponseEntity.ok(new ApiResponse<>("비밀번호 수정 성공", userResponse));
    }

    @Operation(summary = "프로필 이미지 삭제", description = "사용자 프로필 이미지를 삭제합니다.")
    @DeleteMapping("/profile-image")
    public ResponseEntity<ApiResponse<UserResponse>> deleteProfileImage(
            @AuthenticationPrincipal UserDetail userDetail) {
        UserResponse userResponse = userService.deleteProfileImage(userDetail.getUserId());
        return ResponseEntity.ok(new ApiResponse<>("프로필 이미지 삭제 성공", userResponse));
    }

    @Operation(summary = "탈퇴", description = "사용자 정보를 삭제합니다.")
    @DeleteMapping()
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal UserDetail userDetail) {
        userService.deleteUser(userDetail.getUserId());
        return ResponseEntity.noContent().build();
    }
}
