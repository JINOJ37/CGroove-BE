package com.example.dance_community.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
public class SignupRequest {
    @Email(message = "이메일 형식 오류")
    @NotBlank(message = "이메일 미입력")
    String email;

    @NotBlank(message = "비밀번호 미입력")
    @Size(min = 4, message = "비밀번호 형식 오류(최소 4자리 이상)")
    String password;

    @NotBlank(message = "이름 미입력")
    String nickname;

    MultipartFile profileImage;
}