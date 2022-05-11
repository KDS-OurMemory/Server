package com.kds.ourmemory.firebase.v2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.firebase.v2.controller.dto.FcmSendMessageReqDto;
import com.kds.ourmemory.firebase.v2.service.FcmV2Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class FcmV2ControllerTest {

    @Mock
    private FcmV2Service fcmV2Service;

    @InjectMocks
    private FcmV2Controller fcmController;

    private final ObjectMapper mapper = new ObjectMapper();

    @Order(1)
    @Test
    void _1_FCM전송_성공() {
        var userId = 1L;
        var fcmSendMessageReqDto = new FcmSendMessageReqDto("test title", "test body");

        // given
        given(fcmV2Service.sendMessage(anyLong(), any())).willReturn(true);

        // when
        var apiResult = fcmController.sendMessage(userId, fcmSendMessageReqDto);

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
