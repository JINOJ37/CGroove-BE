package com.example.cgroove.dto.post;

import com.example.cgroove.validation.ValidScopePost;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidScopePost
public class PostCreateRequest {
    @NotBlank(message = "게시물 공개 범위 미입력")
    private String scope;

    private Long clubId;

    @NotBlank(message = "게시물 제목 미입력")
    private String title;

    @NotBlank(message = "게시물 내용 미입력")
    private String content;

    private List<String> tags;
    private List<String> images;
}