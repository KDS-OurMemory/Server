package com.kds.ourmemory.service.v1.memory;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

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

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryResponseDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryResponseDto;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {
    
    @Autowired private MemoryService memoryService;
    /**
     * 일정을 등록할 방이 있는 경우
     * 참여자가 있는 경우 
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
        
        log.info("CreateDate: {} memoryId: {}", insertResponse_방O_참여자O.getAddDate(), insertResponse_방O_참여자O.getId());
    }
    
    @Test
    @Order(2)
    void 방O_참여자O_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방O_참여자O.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(3)
    void 방O_참여자X_일정_생성() throws CMemoryException {
        insertResponse_방O_참여자X = memoryService.insert(insertRequest_방O_참여자X);
        assertThat(insertResponse_방O_참여자X).isNotNull();
        assertThat(insertResponse_방O_참여자X.getAddDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} memoryId: {}", insertResponse_방O_참여자X.getAddDate(), insertResponse_방O_참여자X.getId());
    }
    
    @Test
    @Order(4)
    void 방O_참여자X_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방O_참여자X.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(5)
    void 방X_참여자O_일정_생성() throws CMemoryException {
        insertResponse_방X_참여자O = memoryService.insert(insertRequest_방X_참여자O);
        assertThat(insertResponse_방X_참여자O).isNotNull();
        assertThat(insertResponse_방X_참여자O.getAddDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} memoryId: {}", insertResponse_방O_참여자O.getAddDate(), insertResponse_방X_참여자O.getId());
    }
    
    @Test
    @Order(6)
    void 방X_참여자O_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방X_참여자O.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(7)
    void 방X_참여자X_일정_생성() throws CMemoryException {
        insertResponse_방X_참여자X = memoryService.insert(insertRequest_방X_참여자X);
        assertThat(insertResponse_방X_참여자X).isNotNull();
        assertThat(insertResponse_방X_참여자X.getAddDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} memoryId: {}", insertResponse_방O_참여자O.getAddDate(), insertResponse_방X_참여자X.getId());
    }
    
    @Test
    @Order(8)
    void 방X_참여자X_일정_삭제() throws CRoomException {
        DeleteMemoryResponseDto deleteMemoryResponseDto = memoryService.delete(insertResponse_방X_참여자X.getId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
