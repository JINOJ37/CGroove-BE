package com.example.dance_community.controller;

import com.example.dance_community.dto.ApiResponse;
import com.example.dance_community.dto.auth.*;
import com.example.dance_community.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1_Auth", description = "계정 관련 API")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 사용자 이름을 입력 받아 회원가입합니다.")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @ModelAttribute SignupRequest request
    ) {
        AuthResponse authResponse = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("회원가입 성공", authResponse));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력 받아 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그인 성공", authResponse));
    }

    @Operation(summary = "토큰 재발급", description = "토큰이 만료됐을 때 재발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        AuthResponse authResponse = authService.refresh(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>("토큰 재발급 성공", authResponse));
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletResponse response
    ) {
        authService.logout(response);
        return ResponseEntity.ok(new ApiResponse<>("로그아웃 성공", null));
    }
}