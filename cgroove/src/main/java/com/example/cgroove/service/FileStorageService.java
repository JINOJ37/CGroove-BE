package com.example.cgroove.service;

import com.example.cgroove.config.FileProperties;
import com.example.cgroove.entity.ImageHolder;
import com.example.cgroove.enums.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileProperties fileProperties;

    public String saveImage(MultipartFile file, ImageType type) {
        if (file == null || file.isEmpty()) {
            if (type.allowDefault()) {
                return null;
            } else {
                throw new IllegalArgumentException(type.getTypeName() + " 이미지 파일이 없습니다");
            }
        }

        try {
            // Null 체크 및 로깅
            if (fileProperties == null) {
                log.error("FileProperties is null");
                throw new IllegalStateException("FileProperties가 초기화되지 않았습니다");
            }

            String uploadDir = fileProperties.getUploadDir();
            if (uploadDir == null || uploadDir.isBlank()) {
                log.error("Upload directory is null or blank. baseDir: {}", fileProperties.getBaseDir());
                throw new IllegalStateException("업로드 디렉토리가 설정되지 않았습니다");
            }

            String typeDir = type.getDirectory();
            if (typeDir == null || typeDir.isBlank()) {
                log.error("Type directory is null or blank for type: {}", type);
                throw new IllegalStateException("이미지 타입 디렉토리가 설정되지 않았습니다");
            }

            log.info("Saving image - uploadDir: {}, typeDir: {}, filename: {}",
                    uploadDir, typeDir, file.getOriginalFilename());

            String filename = generateFileName(file.getOriginalFilename());

            Path uploadPath = Paths.get(uploadDir, typeDir);
            log.info("Upload path: {}", uploadPath.toAbsolutePath());

            if (!Files.exists(uploadPath)) {
                log.info("Creating directory: {}", uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String result = String.format("/uploads/%s/%s", typeDir, filename);
            log.info("Image saved successfully: {}", result);

            return result;

        } catch (IOException e) {
            log.error("Failed to save {} image: {}", type.getTypeName(), e.getMessage(), e);
            throw new RuntimeException(type.getTypeName() + " 이미지 저장 실패: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            // filePath가 "/uploads/users/xxx.jpg" 형태인 경우
            // 실제 파일 경로: {uploadDir}/users/xxx.jpg
            Path actualPath;
            if (filePath.startsWith("/uploads/")) {
                String relativePath = filePath.substring("/uploads/".length());
                actualPath = Paths.get(fileProperties.getUploadDir(), relativePath);
            } else if (filePath.startsWith("/")) {
                actualPath = Paths.get(fileProperties.getUploadDir(), filePath.substring(1));
            } else {
                actualPath = Paths.get(fileProperties.getUploadDir(), filePath);
            }

            log.info("Deleting file: {} -> {}", filePath, actualPath.toAbsolutePath());

            if (Files.exists(actualPath)) {
                Files.delete(actualPath);
                log.info("File deleted successfully: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", actualPath.toAbsolutePath());
            }

        } catch (IOException e) {
            log.error("Failed to delete file: {} - {}", filePath, e.getMessage(), e);
        }
    }

    private String generateFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        return uuid + "_" + originalFilename;
    }

    void processImageUpdate(ImageHolder entity, List<String> newImages, List<String> keepImages) {
        if (keepImages == null) {
            if (newImages != null && !newImages.isEmpty()) {
                List<String> currentImages = new ArrayList<>(entity.getImages());
                currentImages.addAll(newImages);
                entity.updateImages(currentImages);
            }
            return;
        }

        List<String> currentImages = entity.getImages();
        List<String> finalImages = new ArrayList<>();

        if (keepImages.isEmpty()) {
            for (String imagePath : currentImages) {
                this.deleteFile(imagePath);
            }
        } else {
            finalImages.addAll(keepImages);

            List<String> imagesToDelete = currentImages.stream()
                    .filter(img -> !keepImages.contains(img))
                    .collect(Collectors.toList());

            for (String imagePath : imagesToDelete) {
                this.deleteFile(imagePath);
            }
        }

        if (newImages != null && !newImages.isEmpty()) {
            finalImages.addAll(newImages);
        }
        entity.updateImages(finalImages);
    }
}