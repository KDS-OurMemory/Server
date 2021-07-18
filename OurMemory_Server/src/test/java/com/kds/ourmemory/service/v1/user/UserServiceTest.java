package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.DeviceOs;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private final UserService userService;

    /**
     * Assert time format -> delete sec
     *
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;

    @Autowired
    private UserServiceTest(UserService userService) {
        this.userService = userService;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @DisplayName("회원가입-로그인-단일 조회-토큰변경-업데이트")
    @Transactional
    void signUpSignInFind() {
        /* 0. Create Request */
        InsertUserDto.Request insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        
        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insertRsp.getJoinDate())).isTrue();

        /* 2. Sign in */
        var signInRsp = userService.signIn(insertReq.getSnsType(), insertReq.getSnsId());
        assertThat(signInRsp).isNotNull();
        assertThat(signInRsp.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(signInRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(signInRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(signInRsp.getBirthday()).isEqualTo(insertReq.getBirthday());

        /* 3. Find */
        var findRsp = userService.find(insertRsp.getUserId());
        assertThat(findRsp).isNotNull();
        assertThat(findRsp.getId()).isEqualTo(insertRsp.getUserId());
        assertThat(findRsp.getSnsType()).isEqualTo(insertReq.getSnsType());
        assertThat(findRsp.getSnsId()).isEqualTo(insertReq.getSnsId());
        assertThat(findRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(findRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertThat(findRsp.isSolar()).isEqualTo(insertReq.isSolar());
        assertThat(findRsp.isBirthdayOpen()).isEqualTo(insertReq.isBirthdayOpen());
        assertThat(findRsp.getPushToken()).isEqualTo(insertReq.getPushToken());

        /* 4. Patch token */
        // TODO

        /* 5. Update */
        // TODO
    }

    @Test
    @Order(2)
    @DisplayName("사용자 목록 조회")
    @Transactional
    void findUsers() {
        /* 0. Create Request */
        InsertUserDto.Request insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        /* 1. Insert */
        InsertUserDto.Response insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insertRsp.getJoinDate())).isTrue();
        
        // TODO: 사용자 여러명 추가 후 조회, 동일 이름 포함할 것

        /* 2. Find users */
        // 1) find by id
        var findUsersByIdList = userService.findUsers(insertRsp.getUserId(), null);
        assertThat(findUsersByIdList).isNotNull();
        assertThat(findUsersByIdList.isEmpty()).isFalse();

        var findUsersById = findUsersByIdList.get(0);
        assertThat(findUsersById).isNotNull();
        assertThat(findUsersById.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(findUsersById.getName()).isEqualTo(insertReq.getName());
        assertThat(findUsersById.isSolar()).isEqualTo(insertReq.isSolar());
        assertThat(findUsersById.isBirthdayOpen()).isEqualTo(insertReq.isBirthdayOpen());
        assertThat(findUsersById.getBirthday()).isEqualTo(
                insertReq.isBirthdayOpen()? insertReq.getBirthday() : null
        );

        // 2) find by name
        var findUsersByNameList = userService.findUsers(null, insertReq.getName());
        assertThat(findUsersByNameList).isNotNull();
        assertThat(findUsersByNameList.isEmpty()).isFalse();

        var findUsersByName = findUsersByNameList.get(0);
        assertThat(findUsersByName).isNotNull();
        assertThat(findUsersByName.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(findUsersByName.getName()).isEqualTo(insertReq.getName());
        assertThat(findUsersByName.isSolar()).isEqualTo(insertReq.isSolar());
        assertThat(findUsersByName.isBirthdayOpen()).isEqualTo(insertReq.isBirthdayOpen());
        assertThat(findUsersByName.getBirthday()).isEqualTo(
                insertReq.isBirthdayOpen()? insertReq.getBirthday() : null
        );
    }

    @Test
    @Order(3)
    @DisplayName("사용자 삭제")
    @Transactional
    void delete() {
        /* 0. Create Request */
        InsertUserDto.Request insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        InsertUserDto.Response insRsp = userService.signUp(insertReq);
        assertThat(insRsp).isNotNull();
        assertThat(insRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insRsp.getJoinDate())).isTrue();
        
        // TODO: 사용자에 방(참여방, 개인방, 방장), 일정(개인 일정, 참여방 일정, 개인방 일정) 생성 후 테스트
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
