package com.example.dance_community.service;

import com.example.dance_community.config.FileProperties;
import com.example.dance_community.enums.ImageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @Mock
    private FileProperties fileProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        lenient().when(fileProperties.getBaseDir()).thenReturn(tempDir.toString());
        fileStorageService = new FileStorageService(fileProperties);
    }

    static class FakeEntity implements com.example.dance_community.entity.ImageHolder {
        private List<String> images;

        public FakeEntity(List<String> images) { this.images = new ArrayList<>(images); }

        @Override public List<String> getImages() { return images; }
        @Override public void updateImages(List<String> images) { this.images = images; }
    }

    @Test
    @DisplayName("이미지 저장 성공")
    void saveImage_Success() {
        String filename = "test.jpg";
        MockMultipartFile file = new MockMultipartFile("image", filename, "image/jpeg", "content".getBytes());

        String savedPath = fileStorageService.saveImage(file, ImageType.POST);

        assertThat(savedPath).contains(filename);
    }

    @Test
    @DisplayName("이미지 저장 - 필수가 아닌 타입(PROFILE)은 빈 파일이면 null 반환")
    void saveImage_Optional_File() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile("image", new byte[0]);

        // when
        String result = fileStorageService.saveImage(emptyFile, ImageType.PROFILE);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("이미지 저장 실패 - 빈 파일")
    void saveImage_Fail_Empty_Post() {
        MockMultipartFile emptyFile = new MockMultipartFile("image", new byte[0]);

        assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.saveImage(emptyFile, ImageType.POST)
        );
    }

    @Test
    @DisplayName("이미지 업데이트 - 기존 이미지 삭제 및 새 목록 반영")
    void processImageUpdate_Success() throws IOException {
        // given
        Path oldFile = tempDir.resolve("old.jpg");
        Files.createFile(oldFile);
        String oldPath = oldFile.toAbsolutePath().toString();
        FakeEntity entity = new FakeEntity(List.of(oldPath));

        List<String> newImages = new ArrayList<>();
        List<String> keepImages = new ArrayList<>();

        // when
        fileStorageService.processImageUpdate(entity, newImages, keepImages);

        // then
        assertThat(Files.exists(oldFile)).isFalse();
        assertThat(entity.getImages()).isEmpty();
    }

    @Test
    @DisplayName("이미지 업데이트 - 유지 목록이 Null이면 기존 이미지 보존하고 새 이미지 추가")
    void processImageUpdate_NullKeepImages() {
        // given
        String oldPath = "old.jpg";
        FakeEntity entity = new FakeEntity(List.of(oldPath));

        List<String> newImages = List.of("new.jpg");

        // when
        fileStorageService.processImageUpdate(entity, newImages, null);

        // then
        assertThat(entity.getImages()).hasSize(2);
        assertThat(entity.getImages()).containsExactly(oldPath, "new.jpg");
    }

    @Test
    @DisplayName("이미지 업데이트 - 일부 유지 및 추가")
    void processImageUpdate_KeepAndAdd() throws IOException {
        // given
        Path keepFile = tempDir.resolve("keep.jpg");
        Files.createFile(keepFile);
        String keepPath = keepFile.toAbsolutePath().toString();

        FakeEntity entity = new FakeEntity(List.of(keepPath));

        List<String> newImages = List.of("new1.jpg", "new2.jpg");
        List<String> keepImages = List.of(keepPath);

        // when
        fileStorageService.processImageUpdate(entity, newImages, keepImages);

        // then
        assertThat(Files.exists(keepFile)).isTrue();
        assertThat(entity.getImages()).hasSize(3);
        assertThat(entity.getImages()).contains("new1.jpg", "new2.jpg");
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFile_Success() throws IOException {
        // given
        Path filePath = tempDir.resolve("delete_me.jpg");
        Files.createFile(filePath);

        assertThat(Files.exists(filePath)).isTrue();

        // when
        fileStorageService.deleteFile(filePath.toAbsolutePath().toString());

        // then
        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    @DisplayName("파일 삭제 - 파일이 존재하지 않아도 에러 안 남")
    void deleteFile_NotExists() {
        // given
        String nonExistentPath = "/path/to/nothing.jpg";

        // when & then
        fileStorageService.deleteFile(nonExistentPath);
    }

    @Test
    @DisplayName("파일 삭제 - 경로가 Null이거나 빈 문자열일 때 무시")
    void deleteFile_NullOrEmpty() {
        // when & then
        fileStorageService.deleteFile(null);
        fileStorageService.deleteFile("");
    }
}