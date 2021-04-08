package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    
    @Autowired private UserService userService;
    
    // insert
    private InsertUserRequestDto insertUserRequestDto;
    private InsertUserResponseDto insertUserResponseDto;
    
    // select
    private UserResponseDto userResponseDto;
    
    // patch
    private PatchUserTokenRequestDto patchUserTokenRequestDto;
    
    // update
    private PutUserRequestDto putUserRequestDto;
    
    @BeforeAll
    void setUp() {
        insertUserRequestDto = new InsertUserRequestDto("TESTS_SNS_ID", 1, "테스트 푸쉬", "테스트 유저", "0730", true, false);
        patchUserTokenRequestDto = new PatchUserTokenRequestDto("testToken");
        putUserRequestDto = new PutUserRequestDto("이름 업데이트!", "0903", true, false);
    }
    
    @Test
    @Order(1)
    void 회원가입() {
        insertUserResponseDto = userService.signUp(insertUserRequestDto.toEntity());
        assertThat(insertUserResponseDto).isNotNull();
        assertThat(insertUserResponseDto.getJoinDate()).isEqualTo(currentDate());
        log.info("joinDate: {}", insertUserResponseDto.getJoinDate());
    }
    
    @Test
    @Order(2)
    void 사용자_조회_snsId() {
        userResponseDto = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userResponseDto.getBirthday()).isEqualTo(userResponseDto.isBirthdayOpen()? insertUserRequestDto.getBirthday() : null);
        
        log.info("userId: {}, userName: {}", userResponseDto.getUserId(), userResponseDto.getName());
    }
    
    @Test
    @Order(3)
    void 사용자_토큰_변경() {
        userResponseDto = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userResponseDto.getBirthday()).isEqualTo(userResponseDto.isBirthdayOpen()? insertUserRequestDto.getBirthday() : null);
        
        log.info("before Token: {}", userResponseDto.getPushToken());
        
        PatchUserTokenResponseDto patchUserTokenResponseDto = userService.patchToken(insertUserResponseDto.getUserId(), patchUserTokenRequestDto) ;
        assertThat(patchUserTokenResponseDto).isNotNull();
        assertThat(patchUserTokenResponseDto.getPatchDate()).isEqualTo(currentDate());
        
        userResponseDto = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getPushToken()).isEqualTo(patchUserTokenRequestDto.getPushToken());
        
        log.info("after Token: {}", userResponseDto.getPushToken());
    }
    
    @Test
    @Order(4)
    void 사용자_수정() {
        log.info("before name:{}, birthday: {}, birthdayOpen: {}, push: {}", userResponseDto.getName(),
                userResponseDto.getBirthday(), userResponseDto.isBirthdayOpen(), userResponseDto.isPush());
        
        PutUserResponseDto putUserResponseDto = userService.update(userResponseDto.getUserId(), putUserRequestDto);
        assertThat(putUserResponseDto).isNotNull();
        assertThat(putUserResponseDto.getUpdateDate()).isEqualTo(currentDate());
        
        userResponseDto = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getBirthday()).isEqualTo(userResponseDto.isBirthdayOpen()? putUserRequestDto.getBirthday(): null);   // 생일 비공개일 경우 값을 생일이 null이 된다.
        assertThat(userResponseDto.getName()).isEqualTo(putUserRequestDto.getName());
        assertThat(userResponseDto.isBirthdayOpen()).isEqualTo(putUserRequestDto.getBirthdayOpen());
        assertThat(userResponseDto.isPush()).isEqualTo(putUserRequestDto.getPush());
        
        log.info("after name:{}, birthday: {}, birthdayOpen: {}, push: {}", userResponseDto.getName(),
                userResponseDto.getBirthday(), userResponseDto.isBirthdayOpen(), userResponseDto.isPush());
    }

    @Test
    @Order(5)
    void 사용자_삭제() {
        DeleteUserResponseDto deleteUserResponseDto = userService.delete(userResponseDto.getUserId());
        assertThat(deleteUserResponseDto).isNotNull();
        assertThat(deleteUserResponseDto.getDeleteDate()).isEqualTo(currentDate());
        
        log.info("deleteDate: {}", deleteUserResponseDto.getDeleteDate());
    }
}
