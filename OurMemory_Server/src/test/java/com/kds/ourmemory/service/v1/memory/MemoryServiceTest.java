package com.kds.ourmemory.service.v1.memory;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryResponseDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {
    
    @Autowired private RoomService roomService;
    private InsertRoomRequestDto insertRoomRequestDto;
    private InsertRoomResponseDto insertRoomResponseDto;

    @Autowired private MemoryService memoryService;
    private InsertMemoryResponseDto insertMemoryResponseDto;
    
    @BeforeAll
    void setUp() {
        insertRoomRequestDto = new InsertRoomRequestDto("테스트방", 94L, false, null);
    }
    
    @Test
    @Order(1)
    void Date_변환() throws IOException{
        String json = "{"
                + "\"roomId\":61,"
                + "\"snsId\":\"202\","
                + "\"name\":\"테스트 일정\","
                + "\"members\":[2,4],"
                + "\"contents\":\"테스트 내용\","
                + "\"place\":\"테스트 장소\","
                + "\"startDate\":\"2021-03-23 19:00\","
                + "\"endDate\":\"2021-03-23 21:00\","
                + "\"bgColor\":\"#FFFFFF\","
                + "\"roomIds\":[60]"
                + "}";
        
        ObjectMapper objectMapper = new ObjectMapper();
        InsertMemoryRequestDto requestDto = objectMapper.readValue(json, InsertMemoryRequestDto.class);
//        assertThat(requestDto.getRoomId(), equalTo(61L));
        log.debug(requestDto.getRoomId() + "");
    }
    
    @Test
    @Order(1)
    void 방_생성() {
        insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
        assertThat(insertRoomResponseDto).isNotNull();
        assertThat(insertRoomResponseDto.getCreateDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} roomId: {}", insertRoomResponseDto.getCreateDate(), insertRoomResponseDto.getId());
    }
    
    @Test
    @Order(2)
    void 일정_생성() throws CMemoryException, ParseException {
        List<Long> members = new ArrayList<>();
        members.add(2L);
        members.add(4L);
        
        InsertMemoryRequestDto insertMemoryRequestDto = new InsertMemoryRequestDto(
                insertRoomResponseDto.getId(),
                "TEST_SNSID", 
                "테스트 일정",
                members,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-22 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-22 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-21 00:00"),   // 첫 번째 알림
                null,   // 두 번째 알림
                "#FFFFFF",
                null
                );  // 배경색
        
        insertMemoryResponseDto = memoryService.insert(insertMemoryRequestDto);
        assertThat(insertMemoryResponseDto).isNotNull();
        assertThat(insertMemoryResponseDto.getAddDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} roomId: {}", insertMemoryResponseDto.getAddDate(), insertMemoryResponseDto.getId());
    }
    
    @Test
    @Order(3)
    void 일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertMemoryResponseDto.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(4)
    void 방_삭제() throws CRoomException {
        DeleteRoomResponseDto deleteRoomResponseDto = roomService.delete(insertRoomResponseDto.getId());
        
        assertThat(deleteRoomResponseDto).isNotNull();
        assertThat(deleteRoomResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
