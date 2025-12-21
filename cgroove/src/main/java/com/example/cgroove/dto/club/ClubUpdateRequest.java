package com.example.cgroove.dto.club;

import com.example.cgroove.enums.ClubType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
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

}
