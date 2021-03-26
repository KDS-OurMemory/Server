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
import com.kds.ourmemory.entity.memory.Memory;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {
    
    @Autowired private MemoryService memoryService;
    
    /**
     * _____________________________________
     * |참여중인 방|        참여자      |방 생성여부|
     * |=====================================|
     * |    O   |0 <= 참여자 <= 방 인원|   X    |
     * |    O   |0 < 참여자 != 방 인원 |   O    |
     * |    X   |0 < 참여자 <= 방 인원 |   O    |
     * |    X   |0 <= 참여자 <= 방 인원|   O    |
     * |    X   |0 == 참여자         |   X    |
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */
    
    
    /**
     * 참여증인 방 O | 참여자 O | (방에)포함O
     */
    private InsertMemoryRequestDto insertRequest_방O_참여자O;
    private InsertMemoryResponseDto insertResponse_방O_참여자O;
    
    /**
     * 일정을 등록할 방이 있는 경우
     * 참여자가 없는 경우
     */
    private InsertMemoryRequestDto insertRequest_방O_참여자X;
    private InsertMemoryResponseDto insertResponse_방O_참여자X;
    
    /**
     * 일정을 등록할 방이 없는 경우
     * 참여자 있는 경우
     */
    private InsertMemoryRequestDto insertRequest_방X_참여자O;
    private InsertMemoryResponseDto insertResponse_방X_참여자O;
    
    /**
     * 일정을 등록할 방이 없는 경우
     * 참여자 없는 경우
     */
    private InsertMemoryRequestDto insertRequest_방X_참여자X;
    private InsertMemoryResponseDto insertResponse_방X_참여자X;
    
    @BeforeAll
    void setUp() throws ParseException {
        List<Long> members = new ArrayList<>();
        members.add(98L);
        
        List<Long> roomIds = new ArrayList<>();
        roomIds.add(64L);
        
        insertRequest_방O_참여자O = new InsertMemoryRequestDto(
                "19930724",
                64L,
                "테스트 일정",
                members,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                roomIds     // 공유할 방
                );
        
        
        insertRequest_방O_참여자X = new InsertMemoryRequestDto(
                "19930724",
                64L,
                "테스트 일정",
                null,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                roomIds     // 공유할 방
                );
        
        insertRequest_방X_참여자O = new InsertMemoryRequestDto(
                "19930724",
                null,
                "테스트 일정",
                members,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                roomIds     // 공유할 방
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
                roomIds     // 공유할 방
                );
    }
    
    @Test
    @Order(1)
    void 방O_참여자O_일정_생성() throws CMemoryException {
        insertResponse_방O_참여자O = memoryService.insert(insertRequest_방O_참여자O);
        assertThat(insertResponse_방O_참여자O).isNotNull();
        assertThat(insertResponse_방O_참여자O.getAddDate()).isEqualTo(currentDate());
        
        log.info("[방O_참여자O] CreateDate: {} memoryId: {}", insertResponse_방O_참여자O.getAddDate(), insertResponse_방O_참여자O.getId());
    }
    
    @Test
    @Order(2)
    @Transactional
    void 방O_참여자O_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자O.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자O_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(3)
    void 방O_참여자O_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방O_참여자O.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(4)
    void 방O_참여자X_일정_생성() throws CMemoryException {
        insertResponse_방O_참여자X = memoryService.insert(insertRequest_방O_참여자X);
        assertThat(insertResponse_방O_참여자X).isNotNull();
        assertThat(insertResponse_방O_참여자X.getAddDate()).isEqualTo(currentDate());
        
        log.info("[방O_참여자X_일정_생성] CreateDate: {} memoryId: {}", insertResponse_방O_참여자X.getAddDate(), insertResponse_방O_참여자X.getId());
    }
    
    @Test
    @Order(5)
    @Transactional
    void 방O_참여자X_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자X.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(6)
    void 방O_참여자X_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방O_참여자X.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(7)
    void 방X_참여자O_일정_생성() throws CMemoryException {
        insertResponse_방X_참여자O = memoryService.insert(insertRequest_방X_참여자O);
        assertThat(insertResponse_방X_참여자O).isNotNull();
        assertThat(insertResponse_방X_참여자O.getAddDate()).isEqualTo(currentDate());
        
        log.info("[방X_참여자O_일정_생성] CreateDate: {} memoryId: {}", insertResponse_방O_참여자O.getAddDate(), insertResponse_방X_참여자O.getId());
    }
    
    @Test
    @Order(8)
    @Transactional
    void 방X_참여자O_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방X_참여자O.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방X_참여자O_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(9)
    void 방X_참여자O_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방X_참여자O.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(10)
    void 방X_참여자X_일정_생성() throws CMemoryException {
        insertResponse_방X_참여자X = memoryService.insert(insertRequest_방X_참여자X);
        assertThat(insertResponse_방X_참여자X).isNotNull();
        assertThat(insertResponse_방X_참여자X.getAddDate()).isEqualTo(currentDate());
        
        log.info("[방X_참여자X_일정_생성] CreateDate: {} memoryId: {}", insertResponse_방O_참여자O.getAddDate(), insertResponse_방X_참여자X.getId());
    }
    
    @Test
    @Order(11)
    @Transactional
    void 방X_참여자X_일정_조회() {
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방X_참여자X.getSnsId());
        assertThat(responseList).isNotNull();
        
        log.info("[방X_참여자X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(12)
    void 방X_참여자X_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방X_참여자X.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
