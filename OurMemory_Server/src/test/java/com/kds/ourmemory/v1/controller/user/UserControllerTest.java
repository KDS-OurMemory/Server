package com.kds.ourmemory.v1.controller.user;

import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
class UserControllerTest {

    @Autowired
    private UserController userController;

    private MockMultipartFile file;

    @BeforeAll
    void setFile() throws Exception{
        file = new MockMultipartFile("request",
                "CD 명함사이즈.jpg",
                "image/jpg",
                new FileInputStream("F:\\자료\\문서\\서류 및 신분증 사진\\CD 명함사이즈.jpg"));
    }

    @Test
    @DisplayName("프로필사진 업로드/삭제")
    void uploadProfileImage() {
       var request = UserReqDto.builder()
               .profileImage(file)
               .build();

        var uploadResponse = userController.uploadProfileImage(287, request);
        assertThat(uploadResponse.getResultCode()).isEqualTo("00");

        var deleteResponse = userController.deleteProfileImage(287);
        assertThat(deleteResponse.getResultCode()).isEqualTo("00");
    }

}
