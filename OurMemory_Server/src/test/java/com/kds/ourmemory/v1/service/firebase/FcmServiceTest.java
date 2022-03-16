package com.kds.ourmemory.v1.service.firebase;

import com.kds.ourmemory.v1.controller.firebase.dto.FcmDto;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class FcmServiceTest {

    private final FcmService fcmService;
    private FcmDto.Request fcmRequestDto;

    @Autowired
    private FcmServiceTest(FcmService fcmService) {
        this.fcmService = fcmService;
    }
    
    @BeforeAll
    void setUp() {
        String token = "exj2fhjwRjafyiNH5y_pHF:APA91bGr3ol9N3WtFR_4ad9z7eP6VLDxeB0pN8rBp7TOgfW1lFTDQ514S9xCysxUCPdL4m1jFdA5a8GJ03MsuToSDvYg34lt4kCugv06WVBfTeXK1Yq0kLgf7IJUDIAowhA9eQrf29E1";
        
        fcmRequestDto = new FcmDto.Request(token, DeviceOs.IOS, "테스트 타이틀", "테스트 바디");
    }
    
    @Test
    void Push() {
        assertTrue(fcmService.sendMessageTo(fcmRequestDto));
    }
}
