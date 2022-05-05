package com.kds.ourmemory.user.v1.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class S3UploaderTest {

    private final S3Uploader s3Uploader;

    @Autowired
    private S3UploaderTest(S3Uploader s3Uploader) {
        this.s3Uploader = s3Uploader;
    }

    @Test
    @DisplayName("파일 업로드/삭제")
    void uploadFile() throws IOException {
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));

        var uploadUrl = s3Uploader.upload(file);
        assertThat(uploadUrl).isNotNull();

        var deleteFile = s3Uploader.delete(uploadUrl);
        assertTrue(deleteFile.isPresent());
    }
}
