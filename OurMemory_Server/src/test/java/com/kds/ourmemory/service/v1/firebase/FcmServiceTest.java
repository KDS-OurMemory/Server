package com.kds.ourmemory.service.v1.firebase;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.controller.v1.firebase.dto.FcmRequestDto;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class FcmServiceTest {

    @Autowired
    private FcmService fcmService;
    
    private FcmRequestDto fcmRequestDto;
    
    @BeforeAll
    void setUp() {
        String token = "d1h25bbirayuirlmzeucai:APA91bHd272ownws5ZvkFnnohTq3QDN0weRlUUqx_XJHBlYZz6F0yxBotIb_7zAr1nMqUKtEOWxJT-Jho5IH0vpiYgHE5GKQQs-1kIK5xhDaWapOoiTTfIN0y0_ayRonBQRxeOKQ7RpS";
        
        fcmRequestDto = new FcmRequestDto(token, "테스트 타이틀", "테스트 바디");
    }
    
    @Test
    void 푸시테스트() {
        fcmService.sendMessageTo(fcmRequestDto);
    }
}
