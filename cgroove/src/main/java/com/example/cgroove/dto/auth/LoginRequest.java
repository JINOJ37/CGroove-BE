package com.example.cgroove.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @Email(message = "이메일 형식 오류")
    @NotBlank(message = "이메일 미입력")
    private String email;

    @NotBlank(message = "비밀번호 미입력")
    private String password;
}