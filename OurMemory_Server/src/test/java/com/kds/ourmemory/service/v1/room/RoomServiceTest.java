package com.kds.ourmemory.service.v1.room;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.controller.v1.room.dto.InsertRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertResponseDto;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoomServiceTest {
    @Autowired private RoomService roomService;
    @Autowired private RoomRepository roomRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private FirebaseCloudMessageService firebaseFcm;
    
    @Test
    @Order(1)
    void 방_생성() {
        String createTime = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
        
        List<Long> member = new ArrayList<>();
        member.add(2L);
        member.add(4L);
        
        InsertRequestDto request = new InsertRequestDto("테스트방", 94L, false, member);
        
        InsertResponseDto response = roomService.insert(request.toEntity(), request.getMember());
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getCreateTime()).isEqualTo(createTime);
        log.info("Found by {}: {}", 1L, response);
    }
}
