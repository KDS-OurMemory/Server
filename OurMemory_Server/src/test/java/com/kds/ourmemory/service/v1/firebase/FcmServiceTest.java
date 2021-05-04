package com.kds.ourmemory.service.v1.firebase;

import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class FcmServiceTest {

    private final FcmService fcmService;
    
    private final String[] OS = {"Android", "iOS"};
    
    private FcmDto.Request fcmRequestDto;

    @Autowired
    private FcmServiceTest(FcmService fcmService) {
        this.fcmService = fcmService;
    }
    
    @BeforeAll
    void setUp() {
        String token = "d1h25BbiRayuirLMzEUCaI:APA91bHd272ownws5ZvkFnnohTq3QDN0weRlUUqx_XJHBlYZz6F0yxBotIb_7zAr1nMqUKtEOWxJT-Jho5IH0vpiYgHE5GKQQs-1kIK5xhDaWapOoiTTfIN0y0_ayRonBQRxeOKQ7RpS";
        
        fcmRequestDto = new FcmDto.Request(token, OS[0], "테스트 타이틀", "테스트 바디");
    }
    
    @Test
    void Push() {
        fcmService.sendMessageTo(fcmRequestDto);
    }
}
