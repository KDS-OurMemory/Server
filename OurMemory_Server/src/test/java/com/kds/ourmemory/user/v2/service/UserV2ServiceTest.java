package com.kds.ourmemory.user.v2.service;

import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v2.controller.dto.UserPatchTokenReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserUpdateReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserUploadProfileImageReqDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserV2ServiceTest {

    private final UserV2Service userV2Service;

    /**
     * Assert time format -> delete sec
     * <p>
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    @Autowired
    private UserV2ServiceTest(UserV2Service userV2Service) {
        this.userV2Service = userV2Service;
    }

    @BeforeAll
    void setUp() {
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Order(1)
    @Test
    void _1_회원가입_성공() {
        /* 0. Create Request */
        var insertReq = UserSignUpReqDto.builder()
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

        /* 1. Insert */
        var insertRsp = userV2Service.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();
        assertThat(insertRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(insertRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(insertRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertThat(insertRsp.isPush()).isEqualTo(insertReq.getPush());
    }

    @Order(2)
    @Test
    void _2_로그인_성공() {
        /* 0. Create Request */
        var insertReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("로그인 성공_SNS_ID")
                .pushToken("로그인 성공 Token")
                .push(true)
                .name("로그인 성공 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var signUpRsp = userV2Service.signUp(insertReq);
        assertThat(signUpRsp).isNotNull();
        assertThat(signUpRsp.getUserId()).isNotNull();

        /* 2. Sign in */
        var signInRsp = userV2Service.signIn(insertReq.getSnsType(), insertReq.getSnsId());
        assertThat(signInRsp).isNotNull();
        assertThat(signInRsp.getUserId()).isEqualTo(signUpRsp.getUserId());
        assertThat(signInRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(signInRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(signInRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertTrue(signInRsp.isPush());
        assertThat(signInRsp.getPrivateRoomId()).isEqualTo(signUpRsp.getPrivateRoomId());
    }

    @Order(3)
    @Test
    void _3_내정보조회_성공() {
        /* 0. Create Request */
        var insertReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("내 정보 조회_SNS_ID")
                .pushToken("내 정보 조회 Token")
                .push(true)
                .name("내 정보 조회 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var insertRsp = userV2Service.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Find */
        var findRsp = userV2Service.find(insertRsp.getUserId());
        assertThat(findRsp).isNotNull();
        assertThat(findRsp.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(findRsp.getSnsType()).isEqualTo(insertReq.getSnsType());
        assertThat(findRsp.getSnsId()).isEqualTo(insertReq.getSnsId());
        assertThat(findRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(findRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertThat(findRsp.isSolar()).isEqualTo(insertReq.getSolar());
        assertThat(findRsp.isBirthdayOpen()).isEqualTo(insertReq.getBirthdayOpen());
        assertThat(findRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
    }

    @Order(4)
    @Test
    void _4_토큰변경_성공() {
        /* 0. Create Request */
        var insertReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("토큰변경_SNS_ID")
                .pushToken("토큰변경 Token")
                .push(true)
                .name("토큰변경 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var patchReq = UserPatchTokenReqDto.builder()
                .pushToken("patch token")
                .build();

        /* 1. Insert */
        var insertRsp = userV2Service.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();
        assertThat(insertRsp.getPushToken()).isEqualTo(insertReq.getPushToken());

        /* 2. Patch token */
        var patchRsp = userV2Service.patchToken(insertRsp.getUserId(), patchReq);
        assertThat(patchRsp).isNotNull();
        assertThat(patchRsp.getPushToken()).isEqualTo(patchReq.getPushToken());
    }

    @Order(5)
    @Test
    void _5_업데이트_성공() {
        /* 0. Create Request */
        var insertReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("테스트 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var updateReq = UserUpdateReqDto.builder()
                .name("update name")
                .birthday("0927")
                .solar(false)
                .birthdayOpen(false)
                .push(false)
                .build();

        /* 1. Insert */
        var insertRsp = userV2Service.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Update */
        var updateRsp = userV2Service.update(insertRsp.getUserId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertThat(updateRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(updateRsp.getBirthday()).isEqualTo(updateReq.getBirthday());
        assertThat(updateRsp.isSolar()).isEqualTo(updateReq.getSolar());
        assertThat(updateRsp.isBirthdayOpen()).isEqualTo(updateReq.getBirthdayOpen());
        assertThat(updateRsp.isPush()).isEqualTo(updateReq.getPush());
    }

    @Order(6)
    @Test
    void _6_프로필사진업로드_성공() throws IOException {
        /* 0. Create Request */
        var insertUserReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserUploadProfileImageReqDto.builder().profileImage(file).build();

        /* 1. Insert */
        var insertUserRsp = userV2Service.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getName()).isEqualTo(insertUserReq.getName());
        assertThat(insertUserRsp.getPushToken()).isEqualTo(insertUserReq.getPushToken());
        assertThat(insertUserRsp.getBirthday()).isEqualTo(insertUserReq.getBirthday());
        assertThat(insertUserRsp.isPush()).isEqualTo(insertUserReq.getPush());

        /* 2. Upload profile image */
        var profileImageRsp = userV2Service.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(profileImageRsp).isNotNull();
        assertThat(profileImageRsp.getProfileImageUrl()).isNotNull();
    }

    @Order(7)
    @Test
    void _7_프로필사진삭제_성공() throws IOException {
        /* 0. Create Request */
        var insertUserReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserUploadProfileImageReqDto.builder().profileImage(file).build();

        /* 1. Insert */
        var insertUserRsp = userV2Service.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getName()).isEqualTo(insertUserReq.getName());
        assertThat(insertUserRsp.getPushToken()).isEqualTo(insertUserReq.getPushToken());
        assertThat(insertUserRsp.getBirthday()).isEqualTo(insertUserReq.getBirthday());
        assertThat(insertUserRsp.isPush()).isEqualTo(insertUserReq.getPush());

        /* 2. Upload profile image */
        var profileImageRsp = userV2Service.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(profileImageRsp).isNotNull();
        assertThat(profileImageRsp.getProfileImageUrl()).isNotNull();

        /* 3. Delete profile image */
        var deleteProfileImageRsp = userV2Service.deleteProfileImage(insertUserRsp.getUserId());
        assertThat(deleteProfileImageRsp).isNotNull();
        assertNull(deleteProfileImageRsp.getProfileImageUrl());
    }

    @Order(8)
    @Test
    void _8_사용자삭제_성공() throws Exception {
        /* 0. Create users */
        // 1) user
        var insertUserReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertUserRsp = userV2Service.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        /* 1. Upload ProfileImage */
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserUploadProfileImageReqDto.builder().profileImage(file).build();
        var profileImageRsp = userV2Service.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(profileImageRsp).isNotNull();
        assertThat(profileImageRsp.getProfileImageUrl()).isNotNull();

        /* 2. Delete user */
        var deleteUserRsp = userV2Service.delete(insertUserRsp.getUserId());
        assertNotNull(deleteUserRsp);
    }

}
