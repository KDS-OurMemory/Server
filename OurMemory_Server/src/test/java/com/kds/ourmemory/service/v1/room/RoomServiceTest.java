package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;

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

import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
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
    
    private InsertRoomRequestDto insertRoomRequestDto;
    private InsertRoomResponseDto insertRoomResponseDto;
    
    @BeforeAll
    void setUp() {
        List<Long> member = new ArrayList<>();
        member.add(2L);
        member.add(4L);
        
        insertRoomRequestDto = new InsertRoomRequestDto("테스트방", 94L, false, member);
    }
    
    @Test
    @Order(1)
    void 방_생성() {
        insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
        Assertions.assertThat(insertRoomResponseDto).isNotNull();
        Assertions.assertThat(insertRoomResponseDto.getCreateDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} roomId: {}", insertRoomResponseDto.getCreateDate(), insertRoomResponseDto.getId());
    }
    
    @Test
    @Order(2)
    void 방_목록_조회() throws CUserNotFoundException {
        User user = userService.findById(insertRoomRequestDto.getOwner()).orElseThrow(() -> new CUserNotFoundException("Not Found User: " + insertRoomRequestDto.getOwner()));
        List<Room> responseList = roomService.findRooms(user.getSnsId());
        Assertions.assertThat(responseList).isNotNull();
        
        log.info("responseList : {}", responseList);    // lazy load 때문인지 실패함. 확인 필요.
    }
    
    @Test
    @Order(3)
    void 방_삭제() throws CRoomException {
        DeleteRoomResponseDto deleteRoomResponseDto = roomService.delete(insertRoomResponseDto.getId());
        
        Assertions.assertThat(deleteRoomResponseDto).isNotNull();
        Assertions.assertThat(deleteRoomResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
