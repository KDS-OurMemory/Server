package com.kds.ourmemory.service.v1.firebase;

import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.entity.user.DeviceOs;
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
    private FcmDto.Request fcmRequestDto;

    @Autowired
    private FcmServiceTest(FcmService fcmService) {
        this.fcmService = fcmService;
    }
    
    @BeforeAll
    void setUp() {
        String token = "deqEWzWZ7Ul9oqQaWfYO6d:APA91bHGA9JLT4aOyn6iHOcY-glkBusIYAIWnv9VDT2nZrtdMlrtXzgQUkLvd_faX0XUk9WCLJ8pgMgVY8nhjWQhviJAm_L4-zqJdZwp4CNmWv0w7BjOrs2nfKSXzQzUHoh88tI1uFfN";
        
        fcmRequestDto = new FcmDto.Request(token, DeviceOs.IOS, "테스트 타이틀", "테스트 바디");
    }
    
    @Test
    void Push() {
        fcmService.sendMessageTo(fcmRequestDto);
    }
}
