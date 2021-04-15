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
    
    private final String OS_ANDROID = "Android";
    private final String OS_iOS = "iOS";
    
    private FcmRequestDto fcmRequestDto;
    
    @BeforeAll
    void setUp() {
        String token = "e6KA7UP6zkPPlWML-vRQSe:APA91bGCUDyclRT8HrJjcB83GGuuR0a7y9V_SiqbgBWV-rfd9sx2JdCD9UORbmpWDb6QR3PK5hAFGlntN5wlR-8t76_dgiLwc8BHryKJu55eVeB96Z2KbHEowxHDDb77ycCxX08f_BAW";
        
        fcmRequestDto = new FcmRequestDto(token, OS_ANDROID, "테스트 타이틀", "테스트 바디");
    }
    
    @Test
    void 푸시테스트() {
        fcmService.sendMessageTo(fcmRequestDto);
    }
}
