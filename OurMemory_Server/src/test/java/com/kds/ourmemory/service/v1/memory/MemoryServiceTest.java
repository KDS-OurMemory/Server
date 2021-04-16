package com.kds.ourmemory.service.v1.memory;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryInternalServerException;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {
    
    @Autowired private MemoryService memoryService;
    
    @Autowired private UserRepository userRepo; // 사용자를 생성하고 삭제하기 위해 추가
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
    
    @Test
    @Order(1)
    @Transactional
    void 방O_참여자O_포함O_일정_생성_조회_삭제() throws MemoryInternalServerException, ParseException {
        /**
         * 0-1. 생성자, 참여자 생성
         */
        User 생성자 = userRepo.save(
                User.builder()
                    .snsId("생성자_snsId")
                    .snsType(1)
                    .pushToken("생성자 토큰")
                    .name("생성자")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User 참여자_포함O = userRepo.save(
                User.builder()
                    .snsId("참여자_포함O_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함O 토큰")
                    .name("참여자_포함O")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User 참여자_포함X = userRepo.save(
                User.builder()
                    .snsId("참여자_포함X_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함X 토큰")
                    .name("참여자_포함X")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        /**
         * 0-2. 메인방, 공유방 생성
         */
        List<Long> 메인방_참여자 = new ArrayList<>();
        메인방_참여자.add(참여자_포함O.getId());
        InsertRoomDto.Response 메인방 = roomService.insert(new InsertRoomDto.Request("메인방", 생성자.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방1 = roomService.insert(new InsertRoomDto.Request("공유방1", 참여자_포함O.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방2 = roomService.insert(new InsertRoomDto.Request("공유방2", 참여자_포함X.getId(), false, 메인방_참여자));
        
        List<Long> 공유방_목록 = new ArrayList<>();
        공유방_목록.add(공유방1.getRoomId());
        공유방_목록.add(공유방2.getRoomId());
        
        /**
         * 0-3. 요청 생성
         */
        List<Long> member_방O_참여자O_포함O = new ArrayList<>();
        member_방O_참여자O_포함O.add(참여자_포함O.getId());
        InsertMemoryDto.Request insertRequest_방O_참여자O_포함O = new InsertMemoryDto.Request(
                생성자.getId(),
                메인방.getRoomId(),
                "테스트 일정",
                member_방O_참여자O_포함O,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                공유방_목록     // 공유할 방
                );
        
        /**
         * 1. 일정 생성
         */
        InsertMemoryDto.Response insertResponse_방O_참여자O_포함O = memoryService.insert(insertRequest_방O_참여자O_포함O);
        assertThat(insertResponse_방O_참여자O_포함O).isNotNull();
        assertThat(insertResponse_방O_참여자O_포함O.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방O_참여자O_포함O.getRoomId()).isEqualTo(insertRequest_방O_참여자O_포함O.getRoomId());
        
        log.info("[방O_참여자O_포함O] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_방O_참여자O_포함O.getAddDate(),
                insertResponse_방O_참여자O_포함O.getMemoryId(), insertResponse_방O_참여자O_포함O.getRoomId());
        
        
        /**
         * 2. 일정 조회
         */
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자O_포함O.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자O_포함O_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
        
        /**
         * 3. 일정 삭제
         */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_방O_참여자O_포함O.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(2)
    @Transactional
    void 방O_참여자O_포함X_일정_생성_조회_삭제() throws MemoryInternalServerException, ParseException {
        /**
         * 0-1. 생성자, 참여자 생성
         */
        User 생성자 = userRepo.save(
                User.builder()
                    .snsId("생성자_snsId")
                    .snsType(1)
                    .pushToken("생성자 토큰")
                    .name("생성자")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User 참여자_포함O = userRepo.save(
                User.builder()
                    .snsId("참여자_포함O_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함O 토큰")
                    .name("참여자_포함O")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User 참여자_포함X = userRepo.save(
                User.builder()
                    .snsId("참여자_포함X_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함X 토큰")
                    .name("참여자_포함X")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        /**
         * 0-2. 메인방, 공유방 생성
         */
        List<Long> 메인방_참여자 = new ArrayList<>();
        메인방_참여자.add(참여자_포함O.getId());
        InsertRoomDto.Response 메인방 = roomService.insert(new InsertRoomDto.Request("메인방", 생성자.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방1 = roomService.insert(new InsertRoomDto.Request("공유방1", 참여자_포함O.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방2 = roomService.insert(new InsertRoomDto.Request("공유방2", 참여자_포함X.getId(), false, 메인방_참여자));
        
        List<Long> 공유방_목록 = new ArrayList<>();
        공유방_목록.add(공유방1.getRoomId());
        공유방_목록.add(공유방2.getRoomId());
        
        /**
         * 0-3. 요청 생성
         */
        List<Long> member_방O_참여자O_포함X = new ArrayList<>();
        member_방O_참여자O_포함X.add(참여자_포함X.getId());
        InsertMemoryDto.Request insertRequest_방O_참여자O_포함X = new InsertMemoryDto.Request(
                생성자.getId(),
                메인방.getRoomId(),
                "테스트 일정",
                member_방O_참여자O_포함X,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                공유방_목록     // 공유할 방
                );
        
        /**
         * 1. 일정 생성
         */
        InsertMemoryDto.Response insertResponse_방O_참여자O_포함X = memoryService.insert(insertRequest_방O_참여자O_포함X);
        assertThat(insertResponse_방O_참여자O_포함X).isNotNull();
        assertThat(insertResponse_방O_참여자O_포함X.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방O_참여자O_포함X.getRoomId()).isNotEqualTo(insertRequest_방O_참여자O_포함X.getRoomId());
        
        log.info("[방O_참여자O_포함X] CreateDate: {}, memoryId: {}, roomId: {}", insertResponse_방O_참여자O_포함X.getAddDate(),
                insertResponse_방O_참여자O_포함X.getMemoryId(), insertResponse_방O_참여자O_포함X.getRoomId());
        
        /**
         * 2. 일정 조회
         */
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자O_포함X.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자O_포함X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
        
        /**
         * 3. 일정 삭제
         */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_방O_참여자O_포함X.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(3)
    @Transactional
    void 방O_참여자X_일정_생성_조회_삭제() throws MemoryInternalServerException, ParseException {
        /**
         * 0-1. 생성자, 참여자 생성
         */
        User 생성자 = userRepo.save(
                User.builder()
                    .snsId("생성자_snsId")
                    .snsType(1)
                    .pushToken("생성자 토큰")
                    .name("생성자")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User 참여자_포함O = userRepo.save(
                User.builder()
                    .snsId("참여자_포함O_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함O 토큰")
                    .name("참여자_포함O")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User 참여자_포함X = userRepo.save(
                User.builder()
                    .snsId("참여자_포함X_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함X 토큰")
                    .name("참여자_포함X")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        /**
         * 0-2. 메인방, 공유방 생성
         */
        List<Long> 메인방_참여자 = new ArrayList<>();
        메인방_참여자.add(참여자_포함O.getId());
        InsertRoomDto.Response 메인방 = roomService.insert(new InsertRoomDto.Request("메인방", 생성자.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방1 = roomService.insert(new InsertRoomDto.Request("공유방1", 참여자_포함O.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방2 = roomService.insert(new InsertRoomDto.Request("공유방2", 참여자_포함X.getId(), false, 메인방_참여자));
        
        List<Long> 공유방_목록 = new ArrayList<>();
        공유방_목록.add(공유방1.getRoomId());
        공유방_목록.add(공유방2.getRoomId());
        
        /**
         * 0-3. 요청 생성
         */
        InsertMemoryDto.Request insertRequest_방O_참여자X = new InsertMemoryDto.Request(
                생성자.getId(),
                메인방.getRoomId(),
                "테스트 일정",
                null,
                "테스트 내용", 
                "테스트 장소", 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 17:00"), // 시작 시간 
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-26 18:00"), // 종료 시간
                new SimpleDateFormat("yyyy-MM-dd HH:ss").parse("2021-03-25 17:00"), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                공유방_목록     // 공유할 방
                );
        
        /**
         * 1. 일정 생성
         */
        InsertMemoryDto.Response insertResponse_방O_참여자X = memoryService.insert(insertRequest_방O_참여자X);
        assertThat(insertResponse_방O_참여자X).isNotNull();
        assertThat(insertResponse_방O_참여자X.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방O_참여자X.getRoomId()).isEqualTo(insertResponse_방O_참여자X.getRoomId());
        
        log.info("[방O_참여자X] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_방O_참여자X.getAddDate(),
                insertResponse_방O_참여자X.getMemoryId(), insertResponse_방O_참여자X.getRoomId());
        
        /**
         * 2. 일정 조회
         */
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방O_참여자X.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[방O_참여자X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
        
        /**
         * 3. 일정 삭제
         */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_방O_참여자X.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(4)
    @Transactional
    void 방X_참여자O_일정_생성_조회_삭제() throws MemoryInternalServerException, ParseException {
        /**
         * 0-1. 생성자, 참여자 생성
         */
        User 생성자 = userRepo.save(
                User.builder()
                    .snsId("생성자_snsId")
                    .snsType(1)
                    .pushToken("생성자 토큰")
                    .name("생성자")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User 참여자_포함O = userRepo.save(
                User.builder()
                    .snsId("참여자_포함O_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함O 토큰")
                    .name("참여자_포함O")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User 참여자_포함X = userRepo.save(
                User.builder()
                    .snsId("참여자_포함X_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함X 토큰")
                    .name("참여자_포함X")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        /**
         * 0-2. 메인방, 공유방 생성
         */
        List<Long> 메인방_참여자 = new ArrayList<>();
        메인방_참여자.add(참여자_포함O.getId());
        InsertRoomDto.Response 공유방1 = roomService.insert(new InsertRoomDto.Request("공유방1", 참여자_포함O.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방2 = roomService.insert(new InsertRoomDto.Request("공유방2", 참여자_포함X.getId(), false, 메인방_참여자));
        
        List<Long> 공유방_목록 = new ArrayList<>();
        공유방_목록.add(공유방1.getRoomId());
        공유방_목록.add(공유방2.getRoomId());
        
        /**
         * 0-3. 요청 생성
         */
        List<Long> member_방X_참여자O = new ArrayList<>();
        member_방X_참여자O.add(참여자_포함O.getId());
        InsertMemoryDto.Request insertRequest_방X_참여자O = new InsertMemoryDto.Request(
                생성자.getId(),
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
                공유방_목록     // 공유할 방
                );
        
        /**
         * 1. 일정 생성
         */
        InsertMemoryDto.Response insertResponse_방X_참여자O = memoryService.insert(insertRequest_방X_참여자O);
        assertThat(insertResponse_방X_참여자O).isNotNull();
        assertThat(insertResponse_방X_참여자O.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방X_참여자O.getRoomId()).isNotEqualTo(insertRequest_방X_참여자O.getRoomId());
        
        log.info("[방X_참여자O] CreateDate: {}, memoryId: {}, roomId: {}", insertResponse_방X_참여자O.getAddDate(),
                insertResponse_방X_참여자O.getMemoryId(), insertResponse_방X_참여자O.getRoomId());
        
        /**
         * 2. 일정 조회
         */
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방X_참여자O.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[방X_참여자O_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
        
        /**
         * 3. 일정 삭제
         */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_방X_참여자O.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
    
    @Test
    @Order(5)
    @Transactional
    void 방X_참여자X_일정_생성_조회_삭제() throws MemoryInternalServerException, ParseException {
        /**
         * 0-1. 생성자, 참여자 생성
         */
        User 생성자 = userRepo.save(
                User.builder()
                    .snsId("생성자_snsId")
                    .snsType(1)
                    .pushToken("생성자 토큰")
                    .name("생성자")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User 참여자_포함O = userRepo.save(
                User.builder()
                    .snsId("참여자_포함O_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함O 토큰")
                    .name("참여자_포함O")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User 참여자_포함X = userRepo.save(
                User.builder()
                    .snsId("참여자_포함X_snsId")
                    .snsType(2)
                    .pushToken("참여자_포함X 토큰")
                    .name("참여자_포함X")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        /**
         * 0-2. 메인방, 공유방 생성
         */
        List<Long> 메인방_참여자 = new ArrayList<>();
        메인방_참여자.add(참여자_포함O.getId());
        InsertRoomDto.Response 공유방1 = roomService.insert(new InsertRoomDto.Request("공유방1", 참여자_포함O.getId(), false, 메인방_참여자));
        InsertRoomDto.Response 공유방2 = roomService.insert(new InsertRoomDto.Request("공유방2", 참여자_포함X.getId(), false, 메인방_참여자));
        
        List<Long> 공유방_목록 = new ArrayList<>();
        공유방_목록.add(공유방1.getRoomId());
        공유방_목록.add(공유방2.getRoomId());
        
        /**
         * 0-3. 요청 생성
         */
        InsertMemoryDto.Request insertRequest_방X_참여자X = new InsertMemoryDto.Request(
                생성자.getId(),
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
                공유방_목록     // 공유할 방
                );
        
        /**
         * 1. 일정 생성
         */
        InsertMemoryDto.Response insertResponse_방X_참여자X = memoryService.insert(insertRequest_방X_참여자X);
        assertThat(insertResponse_방X_참여자X).isNotNull();
        assertThat(insertResponse_방X_참여자X.getAddDate()).isEqualTo(currentDate());
        assertThat(insertResponse_방X_참여자X.getRoomId()).isNull();
        
        log.info("[방X_참여자X] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_방X_참여자X.getAddDate(),
                insertResponse_방X_참여자X.getMemoryId(), insertResponse_방X_참여자X.getRoomId());
        
        /**
         * 2. 일정 조회
         */
        List<Memory> responseList = memoryService.findMemorys(insertRequest_방X_참여자X.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[방X_참여자X_일정_조회]");
        responseList.stream().forEach(memory -> log.info("id: {}, name: {}", memory.getId(), memory.getName()));
        log.info("====================================================================================");
        
        /**
         * 3. 일정 삭제
         */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_방X_참여자X.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(deleteMemoryResponseDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
