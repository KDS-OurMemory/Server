package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
import com.kds.ourmemory.entity.room.Room;
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
        member.add(98L);
        
        insertRoomRequestDto = new InsertRoomRequestDto("테스트방", 99L, false, member);
    }
    
    @Test
    @Order(1)
    void 방_생성() {
        insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
        assertThat(insertRoomResponseDto).isNotNull();
        assertThat(insertRoomResponseDto.getCreateDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} roomId: {}", insertRoomResponseDto.getCreateDate(), insertRoomResponseDto.getRoomId());
    }
    
    @Test
    @Order(2)
    @Transactional
    void 방_목록_조회() throws UserNotFoundException {
        List<Room> responseList = Optional.ofNullable(userService.findUserById(insertRoomRequestDto.getOwner()).get())
            .map(user -> roomService.findRooms(user.getId()))
            .orElseThrow(() -> new RoomInternalServerException("Not Found Room."));
        
        assertThat(responseList).isNotNull();
        
        log.info("[방_목록_조회]");
        responseList.stream().forEach(room -> log.info("id: {}, name: {}", room.getId(), room.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(3)
    void 방_삭제() throws RoomInternalServerException {
        DeleteRoomResponseDto deleteRoomResponseDto = roomService.delete(insertRoomResponseDto.getRoomId());
        
        assertThat(deleteRoomResponseDto).isNotNull();
        assertThat(deleteRoomResponseDto.getDeleteDate()).isEqualTo(currentDate());
        
        log.info("deleteDate: {}", deleteRoomResponseDto.getDeleteDate());
    }
}
