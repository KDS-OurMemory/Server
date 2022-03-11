package com.kds.ourmemory.v1.controller.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.firebase.dto.FcmReqDto;
import com.kds.ourmemory.v1.controller.user.dto.UserRspDto;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.service.firebase.FcmService;
import com.kds.ourmemory.v1.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FcmControllerTest {

    @Mock
    private FcmService fcmService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FcmController fcmController;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("FCM 메시지 전송 요청 | 성공")
    void sendMessageSuccess() {
        var userId = 1L;
        var fcmReqDto = new FcmReqDto("test title", "test body");
        var user = User.builder()
                .id(userId)
                .snsType(1)
                .snsId("test sns id")
                .name("test name")
                .pushToken("test token")
                .push(true)
                .deviceOs(DeviceOs.AOS)
                .build();
        user.updatePrivateRoomId(1L);
        var userRspDto = new UserRspDto(user);

        // given
        given(userService.find(userId)).willReturn(userRspDto);
        given(fcmService.sendMessageTo(any())).willReturn(true);

        // when
        var apiResult = fcmController.sendMessage(userId, fcmReqDto);

        // then
        assertThat(apiResult.getResultCode()).isEqualTo("S001");
        assertTrue(apiResult.getResponse());

        printToPrettyJson(apiResult);
    }

    private void printToPrettyJson(ApiResult apiResult) {
        try {
            log.debug("ApiResult: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiResult));
        } catch (JsonProcessingException e) {
            log.error("printError!", e);
        }
    }

}
