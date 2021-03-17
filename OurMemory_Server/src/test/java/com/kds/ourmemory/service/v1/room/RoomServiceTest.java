package com.kds.ourmemory.service.v1.room;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.InsertRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.service.v1.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomServiceTest {
    @Autowired private RoomService roomService;
    @Autowired private UserService userService;
    
    private InsertRequestDto insertRequestDto;
    
    @BeforeAll
    void setUp() {
        List<Long> member = new ArrayList<>();
        member.add(2L);
        member.add(4L);
        
        insertRequestDto = new InsertRequestDto("테스트방", 94L, false, member);
    }
    
    @Test
    @Order(1)
    void 방_생성() {
        String createTime = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
        
        InsertResponseDto response = roomService.insert(insertRequestDto.toEntity(), insertRequestDto.getMember());
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getCreateTime()).isEqualTo(createTime);
        log.info("Found by {}: {}", 1L, response);
    }
    
    @Test
    @Order(2)
    void 방_조회() throws CUserNotFoundException{
        User user = userService.findById(insertRequestDto.getOwner()).orElseThrow(() -> new CUserNotFoundException("Not Found User: " + insertRequestDto.getOwner()));
        
        List<Room> responseList = roomService.findRooms(user.getSnsId());
        Assertions.assertThat(responseList).isNotNull();
    }
}
