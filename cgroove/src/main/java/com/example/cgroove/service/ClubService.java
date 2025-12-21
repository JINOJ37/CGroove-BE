package com.example.cgroove.service;

import com.example.cgroove.dto.club.ClubCreateRequest;
import com.example.cgroove.dto.club.ClubResponse;
import com.example.cgroove.dto.club.ClubUpdateRequest;
import com.example.cgroove.entity.Club;
import com.example.cgroove.entity.User;
import com.example.cgroove.enums.ClubJoinStatus;
import com.example.cgroove.enums.ClubRole;
import com.example.cgroove.enums.ImageType;
import com.example.cgroove.exception.NotFoundException;
import com.example.cgroove.repository.ClubRepository;
import com.example.cgroove.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubAuthService clubAuthService;
    private final ClubJoinService clubJoinService;
    private final EventJoinService eventJoinService;
    private final PostService postService;
    private final EventService eventService;
    private final FileStorageService fileStorageService;
    private final EntityManager em;

    @Transactional
    public ClubResponse createClub(Long userId, ClubCreateRequest request, MultipartFile clubImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        String clubImagePath = null;
        if (clubImage != null && !clubImage.isEmpty()) {
            clubImagePath = fileStorageService.saveImage(clubImage, ImageType.CLUB);
        }

        Club club = Club.builder()
                .clubName(request.getClubName())
                .intro(request.getIntro())
                .description(request.getDescription())
                .locationName(request.getLocationName())
                .clubType(request.getClubType())
                .clubImage(clubImagePath)
                .tags(request.getTags())
                .build();

        club.addMember(user, ClubRole.LEADER, ClubJoinStatus.ACTIVE);
        return ClubResponse.from(clubRepository.save(club));
    }

    public ClubResponse getClub(Long clubId) {
        return ClubResponse.from(clubAuthService.findByClubId(clubId));
    }
    public List<ClubResponse> getClubs() {
        return clubRepository.findAll().stream().map(ClubResponse::from).toList();
    }

    @Transactional
    public ClubResponse updateClub(Long userId, Long clubId, ClubUpdateRequest request, MultipartFile clubImage) {
        clubAuthService.validateClubAuthority(userId, clubId);
        Club club = clubAuthService.findByClubId(clubId);
        String currentClubImage = club.getClubImage();

        String newClubImagePath = currentClubImage;
        if (clubImage != null && !clubImage.isEmpty()) {
            if (currentClubImage != null) {
                fileStorageService.deleteFile(currentClubImage);
            }
            newClubImagePath = fileStorageService.saveImage(clubImage, ImageType.CLUB);
        }

        club.updateClub(
                request.getClubName(), request.getIntro(), request.getDescription(),
                request.getLocationName(), request.getClubType(),
                newClubImagePath, request.getTags()
        );

        return ClubResponse.from(clubRepository.save(club));
    }

    @Transactional
    public ClubResponse deleteClubImage(Long userId, Long clubId) {
        clubAuthService.validateClubAuthority(userId, clubId);
        Club club = clubAuthService.findByClubId(clubId);

        if (club.getClubImage() != null) {
            fileStorageService.deleteFile(club.getClubImage());
            club.deleteImage();
        }
        return ClubResponse.from(clubRepository.save(club));
    }

    @Transactional
    public void deleteClub(Long userId, Long clubId) {
        clubAuthService.validateLeaderAuthority(userId, clubId);
        Club club = clubAuthService.findByClubId(clubId);

        if (club.getClubImage() != null) {
            fileStorageService.deleteFile(club.getClubImage());
        }

        postService.softDeleteByClubId(clubId);
        eventService.softDeleteByClubId(clubId);
        clubJoinService.softDeleteByClubId(clubId);
        eventJoinService.softDeleteByClubId(clubId);

        club.delete();

        em.flush();
        em.clear();
    }
}
