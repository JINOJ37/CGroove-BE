package com.example.cgroove.dto.post;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    @NotBlank(message = "게시물 제목 미입력")
    private String title;

    @NotBlank(message = "게시물 내용 미입력")
    private String content;

    private List<String> tags;
    private List<String> newImagePaths;
    private List<String> keepImages;
}