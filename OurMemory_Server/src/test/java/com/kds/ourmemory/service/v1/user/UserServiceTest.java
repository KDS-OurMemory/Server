package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.controller.v1.user.dto.*;
import com.kds.ourmemory.entity.BaseTimeEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                false, "Android");
        patchUserTokenRequestDto = new PatchTokenDto.Request("after pushToken");
        putUserRequestDto = new PutUserDto.Request("이름 업데이트!", "0903", true, false);

        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @Transactional
    void SignIn() {
        InsertUserDto.Response insRes = userService.signUp(insertUserRequestDto);
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isNotNull();
        assertThat(isNow(insRes.getJoinDate())).isTrue();
        
        log.info("joinDate: {}", insRes.getJoinDate());
    }

    @Test
    @Order(2)
    @Transactional
    void User_Read() {
        InsertUserDto.Response insRes = userService.signUp(insertUserRequestDto);
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isNotNull();
        assertThat(isNow(insRes.getJoinDate())).isTrue();
        
        log.info("joinDate: {}", insRes.getJoinDate());

        FindUserDto.Response userRes = userService.signIn(insertUserRequestDto.getSnsType(),
                insertUserRequestDto.getSnsId());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRes.getBirthday())
                .isEqualTo(userRes.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);

        log.info("userId: {}, userName: {}", userRes.getUserId(), userRes.getName());
    }

    @Test
    @Order(3)
    @Transactional
    void User_Find() {
        InsertUserDto.Response insRes = userService.signUp(insertUserRequestDto);
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isNotNull();
        assertThat(isNow(insRes.getJoinDate())).isTrue();

        log.info("joinDate: {}", insRes.getJoinDate());

        FindUsersDto.Response userRes = userService.findUsers(insRes.getUserId(), null);
        assertThat(userRes).isNotNull();
        assertThat(userRes.getUsers()).isNotNull();
        assertThat(userRes.getUsers().size()).isEqualTo(1);

        FindUsersDto.Response.PublicUser  publicUser = userRes.getUsers().get(0);
        assertThat(publicUser.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(publicUser.getBirthday())
                .isEqualTo(publicUser.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);

        log.info("find users by userId | userId: {}, userName: {}", publicUser.getUserId(), publicUser.getName());

        userRes = userService.findUsers(null, insertUserRequestDto.getName());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getUsers()).isNotNull();
        assertThat(userRes.getUsers().size()).isEqualTo(1);

        publicUser = userRes.getUsers().get(0);
        assertThat(publicUser.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(publicUser.getBirthday())
                .isEqualTo(publicUser.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);

        log.info("find users by userId | userId: {}, userName: {}", publicUser.getUserId(), publicUser.getName());
    }

    @Test
    @Order(4)
    @Transactional
    void User_Patch_Token() {
        InsertUserDto.Response insRes = userService.signUp(insertUserRequestDto);
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isNotNull();
        assertThat(isNow(insRes.getJoinDate())).isTrue();
        log.info("joinDate: {}", insRes.getJoinDate());

        FindUserDto.Response userRes = userService.signIn(insertUserRequestDto.getSnsType(),
                insertUserRequestDto.getSnsId());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRes.getBirthday())
                .isEqualTo(userRes.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);

        log.info("before Token: {}", userRes.getPushToken());

        PatchTokenDto.Response patchUserTokenResponseDto = userService.patchToken(userRes.getUserId(),
                patchUserTokenRequestDto);
        assertThat(patchUserTokenResponseDto).isNotNull();
        assertThat(isNow(patchUserTokenResponseDto.getPatchDate())).isTrue();

        userRes = userService.signIn(insertUserRequestDto.getSnsType(), insertUserRequestDto.getSnsId());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getPushToken()).isEqualTo(patchUserTokenRequestDto.getPushToken());

        log.info("after Token: {}", userRes.getPushToken());
    }

    @Test
    @Order(5)
    @Transactional
    void User_Update() {
        InsertUserDto.Response insRes = userService.signUp(insertUserRequestDto);
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isNotNull();
        assertThat(isNow(insRes.getJoinDate())).isTrue();
        log.info("joinDate: {}", insRes.getJoinDate());

        FindUserDto.Response userRes = userService.signIn(insertUserRequestDto.getSnsType(),
                insertUserRequestDto.getSnsId());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRes.getBirthday())
                .isEqualTo(userRes.isBirthdayOpen() ? insertUserRequestDto.getBirthday() : null);

        log.info("before name: {}, birthday: {}, birthdayOpen: {}, push: {}", userRes.getName(), userRes.getBirthday(),
                userRes.isBirthdayOpen(), userRes.isPush());

        PutUserDto.Response putUserResponseDto = userService.update(userRes.getUserId(), putUserRequestDto);
        assertThat(putUserResponseDto).isNotNull();
        assertThat(isNow(putUserResponseDto.getUpdateDate())).isTrue();

        userRes = userService.signIn(insertUserRequestDto.getSnsType(), insertUserRequestDto.getSnsId());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getBirthday()).isEqualTo(userRes.isBirthdayOpen() ? putUserRequestDto.getBirthday() : null); // If private, it will be null.
        assertThat(userRes.getName()).isEqualTo(putUserRequestDto.getName());
        assertThat(userRes.isBirthdayOpen()).isEqualTo(putUserRequestDto.getBirthdayOpen());
        assertThat(userRes.isPush()).isEqualTo(putUserRequestDto.getPush());

        log.info("after name: {}, birthday: {}, birthdayOpen: {}, push: {}", userRes.getName(), userRes.getBirthday(),
                userRes.isBirthdayOpen(), userRes.isPush());
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
