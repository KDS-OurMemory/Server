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

import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.SignInResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.SignUpRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.SignUpResponseDto;
import com.kds.ourmemory.entity.user.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    
    @Autowired private UserService userService;
    
    private SignUpRequestDto signUpRequestDto;
    private SignUpResponseDto signUpResponseDto;
    
    private SignInResponseDto signInResponseDto;
    
    @BeforeAll
    void setUp() {
        signUpRequestDto = new SignUpRequestDto("TESTS_SNS_ID", 1, "테스트 푸쉬", "테스트 유저", "0730", true, false);
    }
    
    @Test
    @Order(1)
    void 회원가입() {
        signUpResponseDto = userService.signUp(signUpRequestDto.toEntity());
        assertThat(signUpResponseDto).isNotNull();
        assertThat(signUpResponseDto.getJoinDate()).isEqualTo(currentDate());
        log.info("joinDate: {}", signUpResponseDto.getJoinDate());
    }
    
    @Test
    @Order(2)
    void 사용자_조회_snsId() {
        signInResponseDto = userService.signIn(signUpRequestDto.getSnsId());
        assertThat(signInResponseDto).isNotNull();
        assertThat(signInResponseDto.getName()).isEqualTo(signUpRequestDto.getName());
        assertThat(signInResponseDto.getBirthday()).isEqualTo(signInResponseDto.isBirthdayOpen()? signUpRequestDto.getBirthday() : null);
        
        log.info("userId: {}, userName: {}", signInResponseDto.getId(), signInResponseDto.getName());
    }

    @Test
    @Order(3)
    @Transactional
    void 사용자_조회_userId() {
        User user = userService.findUser(signInResponseDto.getId());
        assertThat(user).isNotNull();
        assertThat(user.getSnsId()).isEqualTo(signUpRequestDto.getSnsId());
        
        log.info("user: {}", user.toString());
    }
    
    @Test
    @Order(4)
    void 사용자_삭제() {
        DeleteUserResponseDto deleteUserResponseDto = userService.delete(signInResponseDto.getId());
        assertThat(deleteUserResponseDto).isNotNull();
        assertThat(deleteUserResponseDto.getDeleteDate()).isEqualTo(currentDate());
        
        log.info("deleteDate: {}", deleteUserResponseDto.getDeleteDate());
    }
}
