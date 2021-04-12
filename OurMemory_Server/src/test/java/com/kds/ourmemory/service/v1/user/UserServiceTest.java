package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    @Transactional
    void 회원가입() {
        InsertUserResponseDto insRes = userService.signUp(insertUserRequestDto.toEntity());
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isEqualTo(currentDate());
        log.info("joinDate: {}", insRes.getJoinDate());
    }
    
    @Test
    @Order(2)
    @Transactional
    void 사용자_조회_snsId() {
        InsertUserResponseDto insRes = userService.signUp(insertUserRequestDto.toEntity());
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isEqualTo(currentDate());
        log.info("joinDate: {}", insRes.getJoinDate());
        
        UserResponseDto userRes = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRes.getBirthday()).isEqualTo(userRes.isBirthdayOpen()? insertUserRequestDto.getBirthday() : null);
        
        log.info("userId: {}, userName: {}", userRes.getUserId(), userRes.getName());
    }
    
    @Test
    @Order(3)
    @Transactional
    void 사용자_토큰_변경() {
        InsertUserResponseDto insRes = userService.signUp(insertUserRequestDto.toEntity());
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isEqualTo(currentDate());
        log.info("joinDate: {}", insRes.getJoinDate());
        
        UserResponseDto userRes = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRes.getBirthday()).isEqualTo(userRes.isBirthdayOpen()? insertUserRequestDto.getBirthday() : null);
        
        log.info("before Token: {}", userRes.getPushToken());
        
        PatchUserTokenResponseDto patchUserTokenResponseDto = userService.patchToken(userRes.getUserId(), patchUserTokenRequestDto) ;
        assertThat(patchUserTokenResponseDto).isNotNull();
        assertThat(patchUserTokenResponseDto.getPatchDate()).isEqualTo(currentDate());
        
        userRes = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getPushToken()).isEqualTo(patchUserTokenRequestDto.getPushToken());
        
        log.info("after Token: {}", userRes.getPushToken());
    }
    
    @Test
    @Order(4)
    @Transactional
    void 사용자_수정() {
        InsertUserResponseDto insRes = userService.signUp(insertUserRequestDto.toEntity());
        assertThat(insRes).isNotNull();
        assertThat(insRes.getJoinDate()).isEqualTo(currentDate());
        log.info("joinDate: {}", insRes.getJoinDate());
        
        UserResponseDto userRes = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getName()).isEqualTo(insertUserRequestDto.getName());
        assertThat(userRes.getBirthday()).isEqualTo(userRes.isBirthdayOpen()? insertUserRequestDto.getBirthday() : null);
        
        log.info("before name:{}, birthday: {}, birthdayOpen: {}, push: {}", userRes.getName(),
                userRes.getBirthday(), userRes.isBirthdayOpen(), userRes.isPush());
        
        PutUserResponseDto putUserResponseDto = userService.update(userRes.getUserId(), putUserRequestDto);
        assertThat(putUserResponseDto).isNotNull();
        assertThat(putUserResponseDto.getUpdateDate()).isEqualTo(currentDate());
        
        userRes = userService.signIn(insertUserRequestDto.getSnsId(), insertUserRequestDto.getSnsType());
        assertThat(userRes).isNotNull();
        assertThat(userRes.getBirthday()).isEqualTo(userRes.isBirthdayOpen()? putUserRequestDto.getBirthday(): null);   // 생일 비공개일 경우 값을 생일이 null이 된다.
        assertThat(userRes.getName()).isEqualTo(putUserRequestDto.getName());
        assertThat(userRes.isBirthdayOpen()).isEqualTo(putUserRequestDto.getBirthdayOpen());
        assertThat(userRes.isPush()).isEqualTo(putUserRequestDto.getPush());
        
        log.info("after name:{}, birthday: {}, birthdayOpen: {}, push: {}", userRes.getName(),
                userRes.getBirthday(), userRes.isBirthdayOpen(), userRes.isPush());
    }
}
