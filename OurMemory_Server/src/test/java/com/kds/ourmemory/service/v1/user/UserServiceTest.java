package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.controller.v1.user.dto.*;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private final UserService userService;

    // Insert
    private InsertUserDto.Request insertUserRequestDto;

    // Patch
    private PatchTokenDto.Request patchUserTokenRequestDto;

    // Update
    private PutUserDto.Request putUserRequestDto;

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
        insertUserRequestDto = new InsertUserDto.Request(1, "TESTS_SNS_ID", "before pushToken", "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID);
        patchUserTokenRequestDto = new PatchTokenDto.Request("after pushToken");
        putUserRequestDto = new PutUserDto.Request("이름 업데이트!", "0903", true, false);

        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @Transactional
    void SignUp() {
        InsertUserDto.Response insRsp = userService.signUp(insertUserRequestDto);
        assertThat(insRsp).isNotNull();
        assertThat(insRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insRsp.getJoinDate())).isTrue();
        
        log.info("joinDate: {}", insRsp.getJoinDate());
    }

    @Test
    @Order(2)
    @Transactional
    void User_SignIn() {
        InsertUserDto.Response insRsp = userService.signUp(insertUserRequestDto);
        assertThat(insRsp).isNotNull();
        assertThat(insRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insRsp.getJoinDate())).isTrue();
        
        log.info("joinDate: {}", insRsp.getJoinDate());

        SignInUserDto.Response userRsp = userService.signIn(insertUserRequestDto.getSnsType(),
                insertUserRequestDto.getSnsId());
        assertThat(userRsp).isNotNull();
        assertThat(userRsp.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRsp.getBirthday())
                .isEqualTo(userRsp.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);

        log.info("userId: {}, userName: {}", userRsp.getUserId(), userRsp.getName());
    }

    @Test
    @Order(3)
    @Transactional
    void User_Find() {
        InsertUserDto.Response insRsp = userService.signUp(insertUserRequestDto);
        assertThat(insRsp).isNotNull();
        assertThat(insRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insRsp.getJoinDate())).isTrue();

        log.info("joinDate: {}", insRsp.getJoinDate());

        List<User> userRsp = userService.findUsers(insRsp.getUserId(), null);
        assertThat(userRsp).isNotNull();
        assertThat(userRsp.size()).isEqualTo(1);

        User foundUser = userRsp.get(0);
        assertThat(foundUser.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(foundUser.getBirthday()).isEqualTo(insertUserRequestDto.getBirthday());

        log.info("find users by userId | userId: {}, userName: {}", foundUser.getId(), foundUser.getName());

        userRsp = userService.findUsers(null, insertUserRequestDto.getName());
        assertThat(userRsp).isNotNull();
        assertThat(userRsp.size()).isEqualTo(1);

        foundUser = userRsp.get(0);
        assertThat(foundUser.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(foundUser.getBirthday()).isEqualTo(insertUserRequestDto.getBirthday());

        log.info("find users by userId | userId: {}, userName: {}", foundUser.getId(), foundUser.getName());
    }

    @Test
    @Order(4)
    @Transactional
    void User_Patch_Token() {
        InsertUserDto.Response insRsp = userService.signUp(insertUserRequestDto);
        assertThat(insRsp).isNotNull();
        assertThat(insRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insRsp.getJoinDate())).isTrue();
        log.info("joinDate: {}", insRsp.getJoinDate());

        SignInUserDto.Response userRsp = userService.signIn(insertUserRequestDto.getSnsType(),
                insertUserRequestDto.getSnsId());
        assertThat(userRsp).isNotNull();
        assertThat(userRsp.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRsp.getBirthday())
                .isEqualTo(userRsp.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);

        log.info("before Token: {}", userRsp.getPushToken());

        PatchTokenDto.Response patchUserTokenResponseDto = userService.patchToken(userRsp.getUserId(),
                patchUserTokenRequestDto);
        assertThat(patchUserTokenResponseDto).isNotNull();
        assertThat(isNow(patchUserTokenResponseDto.getPatchDate())).isTrue();

        userRsp = userService.signIn(insertUserRequestDto.getSnsType(), insertUserRequestDto.getSnsId());
        assertThat(userRsp).isNotNull();
        assertThat(userRsp.getPushToken()).isEqualTo(patchUserTokenRequestDto.getPushToken());

        log.info("after Token: {}", userRsp.getPushToken());
    }

    @Test
    @Order(5)
    @Transactional
    void User_Update() {
        InsertUserDto.Response insRsp = userService.signUp(insertUserRequestDto);
        assertThat(insRsp).isNotNull();
        assertThat(insRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insRsp.getJoinDate())).isTrue();
        log.info("joinDate: {}", insRsp.getJoinDate());

        SignInUserDto.Response userRsp = userService.signIn(insertUserRequestDto.getSnsType(),
                insertUserRequestDto.getSnsId());
        assertThat(userRsp).isNotNull();
        assertThat(userRsp.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRsp.getBirthday())
                .isEqualTo(userRsp.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);
        
        FindUserDto.Response beforeFindUserRsp = userService.find(userRsp.getUserId());

        log.info("before name: {}, birthday: {}, birthdayOpen: {}, push: {}", beforeFindUserRsp.getName(), beforeFindUserRsp.getBirthday(),
                beforeFindUserRsp.isBirthdayOpen(), beforeFindUserRsp.isPush());

        PutUserDto.Response putUserResponseDto = userService.update(userRsp.getUserId(), putUserRequestDto);
        assertThat(putUserResponseDto).isNotNull();
        assertThat(isNow(putUserResponseDto.getUpdateDate())).isTrue();

        userRsp = userService.signIn(insertUserRequestDto.getSnsType(), insertUserRequestDto.getSnsId());
        assertThat(userRsp).isNotNull();
        assertThat(userRsp.getBirthday()).isEqualTo(userRsp.isBirthdayOpen() ? putUserRequestDto.getBirthday() : null); // If private, it will be null.
        assertThat(userRsp.getName()).isEqualTo(putUserRequestDto.getName());
        assertThat(userRsp.isBirthdayOpen()).isEqualTo(putUserRequestDto.getBirthdayOpen());

        FindUserDto.Response afterFindUserRsp = userService.find(userRsp.getUserId());

        log.info("after name: {}, birthday: {}, birthdayOpen: {}, push: {}", afterFindUserRsp.getName(), afterFindUserRsp.getBirthday(),
                afterFindUserRsp.isBirthdayOpen(), afterFindUserRsp.isPush());
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
