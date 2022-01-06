package com.kds.ourmemory.v1.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
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
        var file = new MockMultipartFile("image",
                "CD 명함사이즈.jpg",
                "image/jpg",
                new FileInputStream("F:\\자료\\문서\\서류 및 신분증 사진\\CD 명함사이즈.jpg"));

        var uploadUrl = s3Uploader.upload(file);
        assertThat(uploadUrl).isNotNull();

        var deleteFile = s3Uploader.delete(uploadUrl);
        assertTrue(deleteFile.isPresent());
    }
}
