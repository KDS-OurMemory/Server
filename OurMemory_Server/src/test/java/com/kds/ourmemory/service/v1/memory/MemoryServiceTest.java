package com.kds.ourmemory.service.v1.memory;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryResponseDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {
    
    @Autowired private MemoryService memoryService;
    @Autowired private RoomService roomService; // 일정을 생성하는 과정에서 생긴 방을 삭제하기 위해 추가
    
    /**
     * ______________________________________
     * |참여중인 방|        참여자      |방 생성여부|
     * |=====================================|
     * |    O   |0 < 참여자 <= 방 인원 |   X    |
     * |    O   |0 < 참여자 != 방 인원 |   O    |
     * |    O   |0 == 참여자         |   X    |
     * |    X   |0 < 참여자          |   O    |
     * |    X   |0 == 참여자         |   X    |
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */
    
    
    private InsertMemoryRequestDto insertRequest_방O_참여자O_포함O;
    private InsertMemoryResponseDto insertResponse_방O_참여자O_포함O;
    
    private InsertMemoryRequestDto insertRequest_방O_참여자O_포함X;
    private InsertMemoryResponseDto insertResponse_방O_참여자O_포함X;
    
    private InsertMemoryRequestDto insertRequest_방O_참여자X;
    private InsertMemoryResponseDto insertResponse_방O_참여자X;
    
    private InsertMemoryRequestDto insertRequest_방X_참여자O;
    private InsertMemoryResponseDto insertResponse_방X_참여자O;
    
    private InsertMemoryRequestDto insertRequest_방X_참여자X;
    private InsertMemoryResponseDto insertResponse_방X_참여자X;
    
    @BeforeAll
    void setUp() throws ParseException {
        
        List<Long> member_방O_참여자O_포함O = new ArrayList<>();
        member_방O_참여자O_포함O.add(99L);
        insertRequest_방O_참여자O_포함O = new InsertMemoryRequestDto(
                "19930724",
                80L,
                "테스트 일정",
                member_방O_참여자O_포함O,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null     // 공유할 방
                );
        
        
        List<Long> member_방O_참여자O_포함X = new ArrayList<>();
        member_방O_참여자O_포함X.add(98L);
        insertRequest_방O_참여자O_포함X = new InsertMemoryRequestDto(
                "19930724",
                80L,
                "테스트 일정",
                member_방O_참여자O_포함X,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null     // 공유할 방
                );
        
        
        insertRequest_방O_참여자X = new InsertMemoryRequestDto(
                "19930724",
                80L,
                "테스트 일정",
                null,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null     // 공유할 방
                );
        
        List<Long> member_방X_참여자O = new ArrayList<>();
        member_방X_참여자O.add(98L);
        insertRequest_방X_참여자O = new InsertMemoryRequestDto(
                "19930724",
                null,
                "테스트 일정",
                member_방X_참여자O,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null     // 공유할 방
                );
        
        insertRequest_방X_참여자X = new InsertMemoryRequestDto(
                "19930724",
                null,
                "테스트 일정",
                null,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null     // 공유할 방
                );
    }
    
    @Test
    @Order(1)
    void 방O_참여자O_포함O_일정_생성() throws CMemoryException {
        insertResponse_방O_참여자O_포함O = memoryService.insert(insertRequest_방O_참여자O_포함O);
        assertThat(insertResponse_방O_참여자O_포함O).isNotNull();
        assertThat(insertResponse_방O_참여자O_포함O.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방O_참여자O_포함O.getRoomId()).isEqualTo(insertRequest_방O_참여자O_포함O.getRoomId());
        
        log.info("[방O_참여자O_포함O] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_방O_참여자O_포함O.getAddDate(),
                insertResponse_방O_참여자O_포함O.getMemoryId(), insertResponse_방O_참여자O_포함O.getRoomId());
    }
    
    @Test
    @Order(2)
    @Transactional
    void 방O_참여자O_포함O_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자O_포함O.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자O_포함O_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(3)
    void 방O_참여자O_포함O_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방O_참여자O_포함O.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(4)
    void 방O_참여자O_포함X_일정_생성() throws CMemoryException {
        insertResponse_방O_참여자O_포함X = memoryService.insert(insertRequest_방O_참여자O_포함X);
        assertThat(insertResponse_방O_참여자O_포함X).isNotNull();
        assertThat(insertResponse_방O_참여자O_포함X.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방O_참여자O_포함X.getRoomId()).isNotEqualTo(insertRequest_방O_참여자O_포함X.getRoomId());
        
        log.info("[방O_참여자O_포함X] CreateDate: {}, memoryId: {}, roomId: {}", insertResponse_방O_참여자O_포함X.getAddDate(),
                insertResponse_방O_참여자O_포함X.getMemoryId(), insertResponse_방O_참여자O_포함X.getRoomId());
    }
    
    @Test
    @Order(5)
    @Transactional
    void 방O_참여자O_포함X_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자O_포함X.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자O_포함X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(6)
    void 방O_참여자O_포함X_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방O_참여자O_포함X.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(7)
    void 방O_참여자O_포함X_방_삭제() throws CRoomException {
        DeleteRoomResponseDto deleteRoomResponseDto = roomService.delete(insertResponse_방O_참여자O_포함X.getRoomId());
        
        assertThat(deleteRoomResponseDto).isNotNull();
        assertThat(deleteRoomResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(8)
    void 방O_참여자X_일정_생성() throws CMemoryException {
        insertResponse_방O_참여자X = memoryService.insert(insertRequest_방O_참여자X);
        assertThat(insertResponse_방O_참여자X).isNotNull();
        assertThat(insertResponse_방O_참여자X.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방O_참여자X.getRoomId()).isEqualTo(insertResponse_방O_참여자X.getRoomId());
        
        log.info("[방O_참여자X] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_방O_참여자X.getAddDate(),
                insertResponse_방O_참여자X.getMemoryId(), insertResponse_방O_참여자X.getRoomId());
    }
    
    @Test
    @Order(9)
    @Transactional
    void 방O_참여자X_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자X.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(10)
    void 방O_참여자X_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방O_참여자X.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(11)
    void 방X_참여자O_일정_생성() throws CMemoryException {
        insertResponse_방X_참여자O = memoryService.insert(insertRequest_방X_참여자O);
        assertThat(insertResponse_방X_참여자O).isNotNull();
        assertThat(insertResponse_방X_참여자O.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방X_참여자O.getRoomId()).isNotEqualTo(insertRequest_방X_참여자O.getRoomId());
        
        log.info("[방X_참여자O] CreateDate: {}, memoryId: {}, roomId: {}", insertResponse_방X_참여자O.getAddDate(),
                insertResponse_방X_참여자O.getMemoryId(), insertResponse_방X_참여자O.getRoomId());
    }
    
    @Test
    @Order(12)
    @Transactional
    void 방X_참여자O_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방X_참여자O.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방X_참여자O_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(13)
    void 방X_참여자O_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방X_참여자O.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(14)
    void 방X_참여자O_방_삭제() throws CRoomException {
        DeleteRoomResponseDto deleteRoomResponseDto = roomService.delete(insertResponse_방X_참여자O.getRoomId());
        
        assertThat(deleteRoomResponseDto).isNotNull();
        assertThat(deleteRoomResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(15)
    void 방X_참여자X_일정_생성() throws CMemoryException {
        insertResponse_방X_참여자X = memoryService.insert(insertRequest_방X_참여자X);
        assertThat(insertResponse_방X_참여자X).isNotNull();
        assertThat(insertResponse_방X_참여자X.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방X_참여자X.getRoomId()).isNull();
        
        log.info("[방X_참여자X] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_방X_참여자X.getAddDate(),
                insertResponse_방X_참여자X.getMemoryId(), insertResponse_방X_참여자X.getRoomId());
    }
    
    @Test
    @Order(16)
    @Transactional
    void 방X_참여자X_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방X_참여자X.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방X_참여자X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(17)
    void 방X_참여자X_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방X_참여자X.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
