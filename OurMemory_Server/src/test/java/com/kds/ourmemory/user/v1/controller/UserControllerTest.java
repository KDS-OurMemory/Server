package com.kds.ourmemory.user.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import com.kds.ourmemory.user.v1.controller.dto.UserRspDto;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v1.entity.User;
import com.kds.ourmemory.user.v1.entity.UserRole;
import com.kds.ourmemory.user.v1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("프로필사진 업로드 | 성공")
    void uploadProfileImageSuccess() throws Exception {
        // given
        var user = User.builder()
                .id(1L)
                .snsType(1)
                .snsId("snsId")
                .pushToken("pushToken")
                .push(false)
                .name("name")
                .birthday("0101")
                .solar(true)
                .birthdayOpen(true)
                .role(UserRole.ADMIN)
                .deviceOs(DeviceOs.IOS)
                .build();
        user.updatePrivateRoomId(1L);

        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));

        var request = UserReqDto.builder()
                .profileImage(file)
                .build();

        var mockUploadUrl = "mockUploadS3Url";

        // when
        when(userService.uploadProfileImage(user.getId(), request)).thenReturn(
                new UserRspDto(user.updateProfileImageUrl(mockUploadUrl))
        );

        // then
        var uploadResponse = userController.uploadProfileImage(user.getId(), request);
        assertThat(uploadResponse.getResultCode()).isEqualTo("S001");
        assertThat(uploadResponse.getResponse().getProfileImageUrl()).isEqualTo(mockUploadUrl);
        log.debug("uploadResponse: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(uploadResponse));
    }

    @Test
    @DisplayName("프로필사진 삭제 | 성공")
    void deleteProfileImageSuccess() throws Exception {
        // given
        var user = User.builder()
                .id(1L)
                .snsType(1)
                .snsId("snsId")
                .pushToken("pushToken")
                .push(false)
                .name("name")
                .birthday("0101")
                .solar(true)
                .birthdayOpen(true)
                .role(UserRole.ADMIN)
                .deviceOs(DeviceOs.IOS)
                .build();
        user.updatePrivateRoomId(1L);

        // when
        when(userService.deleteProfileImage(user.getId())).thenReturn(
                new UserRspDto(user.updateProfileImageUrl(null))
        );

        // then
        var deleteResponse = userController.deleteProfileImage(user.getId());
        assertThat(deleteResponse.getResultCode()).isEqualTo("S001");
        assertNull(deleteResponse.getResponse().getProfileImageUrl());
        log.debug("deleteResponse: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deleteResponse));
    }

}
