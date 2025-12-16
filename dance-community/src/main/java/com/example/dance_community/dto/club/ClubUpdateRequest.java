package com.example.dance_community.dto.club;

import com.example.dance_community.enums.ClubType;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ClubUpdateRequest {

    @NotBlank(message = "클럽 이름 미입력")
    private String clubName;

    @NotNull(message = "클럽 타입 미입력")
    private ClubType clubType;

    @NotBlank(message = "클럽 한 줄 소개 미입력")
    private String intro;

    private String description;
    private String locationName;
    private List<String> tags;
    private MultipartFile clubImage;

}