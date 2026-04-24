package com.campus.errand.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void storeImageShouldSaveImageAndReturnPublicUrl() {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "urlPrefix", "/uploads");
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        String url = service.storeImage(file, "avatar", 7L);

        assertThat(url).startsWith("/uploads/avatar/7/").endsWith(".png");
        assertThat(Files.exists(tempDir.resolve("avatar").resolve("7"))).isTrue();
    }

    @Test
    void storeImageShouldRejectNonImage() {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "urlPrefix", "/uploads");
        MockMultipartFile file = new MockMultipartFile("file", "payload.txt", "text/plain", new byte[]{1});

        assertThatThrownBy(() -> service.storeImage(file, "avatar", 7L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("仅支持 JPG、PNG、WEBP 图片");
    }
}
