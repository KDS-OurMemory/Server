package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.room.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {

    private final MemoryService memoryService;
    
    private final UserRepository userRepo; // Add to work with user data
    private final RoomService roomService; // The creation process from adding to the deletion of the room.
    
    /**
     * Assert time format -> delete sec
     * 
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format
    
    /**
     * Test case
     * ______________________________________________________
     * |main room|          Memory member         |Make room|
     * |====================================================|
     * |    O    |0 < Memory member <= room member|    X    |
     * |    O    |0 < Memory member != room member|    O    |
     * |    O    |      0 == Memory member        |    X    |
     * |    X    |      0 < Memory member         |    O    |
     * |    X    |               X                |    X    |
     * ------------------------------------------------------
     */

    @Autowired
    private MemoryServiceTest(MemoryService memoryService, UserRepository userRepo, RoomService roomService) {
        this.memoryService = memoryService;
        this.userRepo = userRepo;
        this.roomService = roomService;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }
    
    @Test
    @Order(1)
    @Transactional
    void RoomO_MemberO() {
        /* 0-1. Create writer, member */
        User Creator = userRepo.save(
                User.builder()
                    .snsId("Creator_snsId")
                    .snsType(1)
                    .pushToken("Creator Token")
                    .name("Creator")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User Member = userRepo.save(
                User.builder()
                    .snsId("Member_snsId")
                    .snsType(2)
                    .pushToken("Member Token")
                    .name("Member")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User Member_IncludeX = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeX_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeX Token")
                    .name("Member_IncludeX")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_Member = new ArrayList<>();
        mainRoom_Member.add(Member.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", Creator.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        List<Long> member = new ArrayList<>();
        member.add(Member.getId());
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                Creator.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                member,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(isNow(insertRsp.getAddDate())).isTrue();
        assertThat(insertRsp.getRoomId()).isEqualTo(insertReq.getRoomId());
        
        log.info("[RoomO_MemberO] CreateDate: {} memoryId: {}, roomId: {}", insertRsp.getAddDate(),
                insertRsp.getMemoryId(), insertRsp.getRoomId());
        
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertReq.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomO_MemberO_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(2)
    @Transactional
    void RoomO_MemberO_IncludeX_Memory() {
        /* 0-1. Create writer, member */
        User Creator = userRepo.save(
                User.builder()
                    .snsId("Creator_snsId")
                    .snsType(1)
                    .pushToken("Creator Token")
                    .name("Creator")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User Member = userRepo.save(
                User.builder()
                    .snsId("Member_snsId")
                    .snsType(2)
                    .pushToken("Member Token")
                    .name("Member")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User Member_IncludeX = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeX_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeX Token")
                    .name("Member_IncludeX")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_Member = new ArrayList<>();
        mainRoom_Member.add(Member.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", Creator.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        List<Long> member = new ArrayList<>();
        member.add(Member_IncludeX.getId());
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                Creator.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                member,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(isNow(insertRsp.getAddDate())).isTrue();
        assertThat(insertRsp.getRoomId()).isNotEqualTo(insertReq.getRoomId());
        
        log.info("[RoomO_MemberO_IncludeX] CreateDate: {}, memoryId: {}, roomId: {}", insertRsp.getAddDate(),
                insertRsp.getMemoryId(), insertRsp.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertReq.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomO_MemberO_IncludeX_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(3)
    @Transactional
    void RoomO_MemberX_Memory() {
        /* 0-1. Create writer, member */
        User Creator = userRepo.save(
                User.builder()
                    .snsId("Creator_snsId")
                    .snsType(1)
                    .pushToken("Creator Token")
                    .name("Creator")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User Member = userRepo.save(
                User.builder()
                    .snsId("Member_snsId")
                    .snsType(2)
                    .pushToken("Member Token")
                    .name("Member")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User Member_IncludeX = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeX_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeX Token")
                    .name("Member_IncludeX")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_Member = new ArrayList<>();
        mainRoom_Member.add(Member.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", Creator.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                Creator.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                null,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(isNow(insertRsp.getAddDate())).isTrue();
        assertThat(insertRsp.getRoomId()).isEqualTo(insertRsp.getRoomId());
        
        log.info("[RoomO_MemberX] CreateDate: {} memoryId: {}, roomId: {}", insertRsp.getAddDate(),
                insertRsp.getMemoryId(), insertRsp.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertReq.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomO_MemberX_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(4)
    @Transactional
    void RoomX_MemberO_Memory() {
        /* 0-1. Create writer, member */
        User Creator = userRepo.save(
                User.builder()
                    .snsId("Creator_snsId")
                    .snsType(1)
                    .pushToken("Creator Token")
                    .name("Creator")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User Member = userRepo.save(
                User.builder()
                    .snsId("Member_snsId")
                    .snsType(2)
                    .pushToken("Member Token")
                    .name("Member")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        User Member_IncludeX = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeX_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeX Token")
                    .name("Member_IncludeX")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_Member = new ArrayList<>();
        mainRoom_Member.add(Member.getId());
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        List<Long> member = new ArrayList<>();
        member.add(Member.getId());
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                Creator.getId(),
                null,
                "Test Memory",
                member,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(isNow(insertRsp.getAddDate())).isTrue();
        assertThat(insertRsp.getRoomId()).isNotEqualTo(insertReq.getRoomId());
        
        log.info("[RoomX_MemberO] CreateDate: {}, memoryId: {}, roomId: {}", insertRsp.getAddDate(),
                insertRsp.getMemoryId(), insertRsp.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertReq.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomX_MemberO_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(5)
    @Transactional
    void RoomX_MemberX_Memory() {
        /* 0-1. Create writer, member */
        User Creator = userRepo.save(
                User.builder()
                    .snsId("Creator_snsId")
                    .snsType(1)
                    .pushToken("Creator Token")
                    .name("Creator")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User Member = userRepo.save(
                User.builder()
                    .snsId("Member_snsId")
                    .snsType(2)
                    .pushToken("Member Token")
                    .name("Member")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("Android")
                    .build());
        
        User Member_IncludeX = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeX_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeX Token")
                    .name("Member_IncludeX")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .used(true)
                    .deviceOs("iOS")
                    .build());
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_Member = new ArrayList<>();
        mainRoom_Member.add(Member.getId());
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                Creator.getId(),
                null,
                "Test Memory",
                null,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(isNow(insertRsp.getAddDate())).isTrue();
        assertThat(insertRsp.getRoomId()).isNull();
        
        log.info("[RoomX_MemberX] CreateDate: {} memoryId: {}, roomId: {}", insertRsp.getAddDate(),
                insertRsp.getMemoryId(), insertRsp.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertReq.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomX_MemberX_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
