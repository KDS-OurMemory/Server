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
import com.kds.ourmemory.controller.v1.room.dto.DeleteResponseDto;
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
    private InsertResponseDto insertResponseDto;
    
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
        insertResponseDto = roomService.insert(insertRequestDto.toEntity(), insertRequestDto.getMember());
        Assertions.assertThat(insertResponseDto).isNotNull();
        Assertions.assertThat(insertResponseDto.getCreateDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} roomId: {}", insertResponseDto.getCreateDate(), insertResponseDto.getId());
    }
    
    @Test
    @Order(2)
    void 방_목록_조회() throws CUserNotFoundException {
        User user = userService.findById(insertRequestDto.getOwner()).orElseThrow(() -> new CUserNotFoundException("Not Found User: " + insertRequestDto.getOwner()));
        List<Room> responseList = roomService.findRooms(user.getSnsId());
        Assertions.assertThat(responseList).isNotNull();
        
        log.info("responseList : {}", responseList);    // lazy load 때문인지 실패함. 확인 필요.
    }
    
    @Test
    @Order(3)
    void 방_삭제() throws CRoomException {
        DeleteResponseDto deleteResponseDto = roomService.delete(insertResponseDto.getId());
        
        Assertions.assertThat(deleteResponseDto).isNotNull();
        Assertions.assertThat(deleteResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
