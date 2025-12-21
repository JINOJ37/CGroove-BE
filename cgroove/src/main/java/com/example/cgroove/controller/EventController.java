package com.example.cgroove.controller;

import com.example.cgroove.dto.ApiResponse;
import com.example.cgroove.dto.event.EventCreateRequest;
import com.example.cgroove.dto.event.EventResponse;
import com.example.cgroove.dto.event.EventUpdateRequest;
import com.example.cgroove.dto.like.EventlikeResponse;
import com.example.cgroove.enums.ImageType;
import com.example.cgroove.security.UserDetail;
import com.example.cgroove.service.EventLikeService;
import com.example.cgroove.service.EventService;
import com.example.cgroove.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "6_Event", description = "행사 관련 API")
public class EventController {
    private final EventService eventService;
    private final EventLikeService eventLikeService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "행사 생성", description = "행사를 새로 만듭니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal UserDetail userDetail,
            @RequestPart("request") @Valid EventCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        List<String> imagePaths = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String path = fileStorageService.saveImage(image, ImageType.EVENT);
                imagePaths.add(path);
            }
        }

        EventCreateRequest eventCreateRequest = EventCreateRequest.builder()
                .scope(request.getScope())
                .clubId(request.getClubId())
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .images(imagePaths)
                .locationName(request.getLocationName())
                .locationAddress(request.getLocationAddress())
                .locationLink(request.getLocationLink())
                .capacity(request.getCapacity())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        EventResponse eventResponse = eventService.createEvent(userDetail.getUserId(), eventCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("행사 생성 성공", eventResponse));
    }

    @Operation(summary = "행사 조회", description = "행사 id를 통해 정보를 불러옵니다.")
    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId
    ) {
        EventResponse eventResponse = eventService.getEvent(eventId, userDetail.getUserId());
        return ResponseEntity.ok(new ApiResponse<>("행사 조회 성공", eventResponse));
    }

    @Operation(summary = "전체 행사 조회", description = "전체 행사 정보를 불러옵니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        List<EventResponse> eventResponseList = eventService.getEvents(userDetail.getUserId());
        return ResponseEntity.ok(new ApiResponse<>("행사 전체 조회 성공", eventResponseList));
    }

    @Operation(summary = "[Upcoming Event]", description = "현재 시간 이후 시작하는 행사를 불러옵니다.")
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents(
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        List<EventResponse> eventResponseList = eventService.getUpcomingEvents(userDetail.getUserId());
        return ResponseEntity.ok(new ApiResponse<>("Upcoming Event 조회 성공", eventResponseList));
    }

    @Operation(summary = "행사 수정", description = "사용자의 행사 정보를 수정합니다.")
    @PatchMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId,
            @RequestPart("request") @Valid EventUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        List<String> newImagePaths = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String path = fileStorageService.saveImage(image, ImageType.EVENT);
                newImagePaths.add(path);
            }
        }

        EventUpdateRequest eventUpdateRequest = EventUpdateRequest.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .keepImages(request.getKeepImages())
                .newImagePaths(newImagePaths)
                .locationName(request.getLocationName())
                .locationAddress(request.getLocationAddress())
                .locationLink(request.getLocationLink())
                .capacity(request.getCapacity())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        EventResponse eventResponse = eventService.updateEvent(userDetail.getUserId(), eventId, eventUpdateRequest);
        return ResponseEntity.ok(new ApiResponse<>("행사 수정 성공", eventResponse));
    }

    @Operation(summary = "행사 삭제", description = "행사 id를 통해 정보를 삭제합니다.")
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId
    ) {
        eventService.deleteEvent(userDetail.getUserId(), eventId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "행사 좋아요", description = "행사 '좋아요' 버튼을 누릅니다.")
    @PostMapping("/{eventId}/like")
    public ResponseEntity<ApiResponse<EventlikeResponse>> toggleLike(
            @AuthenticationPrincipal UserDetail userDetail,
            @PathVariable Long eventId
    ) {
        EventlikeResponse response = eventLikeService.toggleLike(userDetail.getUserId(), eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("행사 좋아요 성공", response));
    }
}
