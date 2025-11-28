package com.example.dance_community.dto.event;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class EventUpdateRequest{
    // 행사 관련 내용 (제목, 내용, 태그, 이미지)
    @NotBlank(message = "행사 제목 미입력")
    private String title;
    @NotBlank(message = "행사 내용 미입력")
    private String content;
    private List<String> tags;
    private List<String> newImagePaths;
    private List<String> keepImages;

    // 행사 장소 정보 (이름, 주소, 링크)
    private String locationName;
    private String locationAddress;
    private String locationLink;

    // 행사 총 수용 인원
    @NotNull(message = "행사 마감인원 미입력")
    private Long capacity;

    // 행사 일시 (시작, 종료 시간)
    @NotNull(message = "행사 시작시간 미입력")
    private LocalDateTime startsAt;
    @NotNull(message = "행사 종료시간 미입력")
    private LocalDateTime endsAt;
}
