package com.kds.ourmemory.firebase.v2.service;

import com.kds.ourmemory.firebase.v2.controller.dto.FcmSendMessageReqDto;
import com.kds.ourmemory.user.v2.enums.DeviceOs;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpReqDto;
import com.kds.ourmemory.user.v2.service.UserV2Service;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FcmV2ServiceTest {

    private final FcmV2Service fcmV2Service;

    private final UserV2Service userV2Service;

    @Autowired
    private FcmV2ServiceTest(FcmV2Service fcmV2Service, UserV2Service userV2Service) {
        this.fcmV2Service = fcmV2Service;
        this.userV2Service = userV2Service;
    }

    @Order(1)
    @Test
    void _1_FCM전송_성공() {
        /* 0. Create User */
        var userSignUpReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("회원가입_SNS_ID")
                .pushToken("회원가입 Token")
                .push(true)
                .name("회원가입 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        var userSignUpRspDto = userV2Service.signUp(userSignUpReq);
        assertThat(userSignUpRspDto).isNotNull();
        assertThat(userSignUpRspDto.getUserId()).isNotNull();
        assertThat(userSignUpRspDto.getName()).isEqualTo(userSignUpReq.getName());
        assertThat(userSignUpRspDto.getPushToken()).isEqualTo(userSignUpReq.getPushToken());
        assertThat(userSignUpRspDto.getBirthday()).isEqualTo(userSignUpReq.getBirthday());
        assertThat(userSignUpRspDto.isPush()).isEqualTo(userSignUpReq.getPush());

        /* 0-2. Create Request */
        var fcmSendMessageReqDto = new FcmSendMessageReqDto("Fcm title", "Fcm contents");

        /* 1. Send Message */
        var isSendMessage = fcmV2Service.sendMessage(userSignUpRspDto.getUserId(), fcmSendMessageReqDto);
        assertTrue(isSendMessage);
    }

}
