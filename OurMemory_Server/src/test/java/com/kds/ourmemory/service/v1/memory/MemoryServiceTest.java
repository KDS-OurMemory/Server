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
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }
    
    @Test
    @Order(1)
    @Transactional
    void RoomO_MemberO_IncludeO_Memory_Create_Read_Delete() {
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
        
        User Member_IncludeO = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeO_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeO Token")
                    .name("Member_IncludeO")
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
        mainRoom_Member.add(Member_IncludeO.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", Creator.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member_IncludeO.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        List<Long> member_RoomO_MemberO_IncludeO = new ArrayList<>();
        member_RoomO_MemberO_IncludeO.add(Member_IncludeO.getId());
        InsertMemoryDto.Request insertReq_RoomO_MemberO_IncludeO = new InsertMemoryDto.Request(
                Creator.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                member_RoomO_MemberO_IncludeO,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2021-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2021-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2021-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertRsp_RoomO_MemberO_IncludeO = memoryService.insert(insertReq_RoomO_MemberO_IncludeO);
        assertThat(insertRsp_RoomO_MemberO_IncludeO).isNotNull();
        assertThat(isNow(insertRsp_RoomO_MemberO_IncludeO.getAddDate())).isTrue();
        assertThat(insertRsp_RoomO_MemberO_IncludeO.getRoomId()).isEqualTo(insertReq_RoomO_MemberO_IncludeO.getRoomId());
        
        log.info("[RoomO_MemberO_IncludeO] CreateDate: {} memoryId: {}, roomId: {}", insertRsp_RoomO_MemberO_IncludeO.getAddDate(),
                insertRsp_RoomO_MemberO_IncludeO.getMemoryId(), insertRsp_RoomO_MemberO_IncludeO.getRoomId());
        
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertReq_RoomO_MemberO_IncludeO.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomO_MemberO_IncludeO_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertRsp_RoomO_MemberO_IncludeO.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(isNow(deleteMemoryResponseDto.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(2)
    @Transactional
    void RoomO_MemberO_IncludeX_Memory_Create_Read_Delete() {
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
        
        User Member_IncludeO = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeO_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeO Token")
                    .name("Member_IncludeO")
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
        mainRoom_Member.add(Member_IncludeO.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", Creator.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member_IncludeO.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        List<Long> member_RoomO_MemberO_IncludeX = new ArrayList<>();
        member_RoomO_MemberO_IncludeX.add(Member_IncludeX.getId());
        InsertMemoryDto.Request insertRequest_RoomO_MemberO_IncludeX = new InsertMemoryDto.Request(
                Creator.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                member_RoomO_MemberO_IncludeX,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2021-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2021-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2021-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertResponse_RoomO_MemberO_IncludeX = memoryService.insert(insertRequest_RoomO_MemberO_IncludeX);
        assertThat(insertResponse_RoomO_MemberO_IncludeX).isNotNull();
        assertThat(isNow(insertResponse_RoomO_MemberO_IncludeX.getAddDate())).isTrue();
        assertThat(insertResponse_RoomO_MemberO_IncludeX.getRoomId()).isNotEqualTo(insertRequest_RoomO_MemberO_IncludeX.getRoomId());
        
        log.info("[RoomO_MemberO_IncludeX] CreateDate: {}, memoryId: {}, roomId: {}", insertResponse_RoomO_MemberO_IncludeX.getAddDate(),
                insertResponse_RoomO_MemberO_IncludeX.getMemoryId(), insertResponse_RoomO_MemberO_IncludeX.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertRequest_RoomO_MemberO_IncludeX.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomO_MemberO_IncludeX_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_RoomO_MemberO_IncludeX.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(isNow(deleteMemoryResponseDto.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(3)
    @Transactional
    void RoomO_MemberX_Memory_Create_Read_Delete() {
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
        
        User Member_IncludeO = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeO_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeO Token")
                    .name("Member_IncludeO")
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
        mainRoom_Member.add(Member_IncludeO.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", Creator.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member_IncludeO.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertRequest_RoomO_MemberX = new InsertMemoryDto.Request(
                Creator.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                null,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2021-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2021-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2021-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertResponse_RoomO_MemberX = memoryService.insert(insertRequest_RoomO_MemberX);
        assertThat(insertResponse_RoomO_MemberX).isNotNull();
        assertThat(isNow(insertResponse_RoomO_MemberX.getAddDate())).isTrue();
        assertThat(insertResponse_RoomO_MemberX.getRoomId()).isEqualTo(insertResponse_RoomO_MemberX.getRoomId());
        
        log.info("[RoomO_MemberX] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_RoomO_MemberX.getAddDate(),
                insertResponse_RoomO_MemberX.getMemoryId(), insertResponse_RoomO_MemberX.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertRequest_RoomO_MemberX.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomO_MemberX_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_RoomO_MemberX.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(isNow(deleteMemoryResponseDto.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(4)
    @Transactional
    void RoomX_MemberO_Memory_Create_Read_Delete() {
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
        
        User Member_IncludeO = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeO_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeO Token")
                    .name("Member_IncludeO")
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
        mainRoom_Member.add(Member_IncludeO.getId());
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member_IncludeO.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        List<Long> member_RoomX_MemberO = new ArrayList<>();
        member_RoomX_MemberO.add(Member_IncludeO.getId());
        InsertMemoryDto.Request insertRequest_RoomX_MemberO = new InsertMemoryDto.Request(
                Creator.getId(),
                null,
                "Test Memory",
                member_RoomX_MemberO,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2021-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2021-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2021-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertResponse_RoomX_MemberO = memoryService.insert(insertRequest_RoomX_MemberO);
        assertThat(insertResponse_RoomX_MemberO).isNotNull();
        assertThat(isNow(insertResponse_RoomX_MemberO.getAddDate())).isTrue();
        assertThat(insertResponse_RoomX_MemberO.getRoomId()).isNotEqualTo(insertRequest_RoomX_MemberO.getRoomId());
        
        log.info("[RoomX_MemberO] CreateDate: {}, memoryId: {}, roomId: {}", insertResponse_RoomX_MemberO.getAddDate(),
                insertResponse_RoomX_MemberO.getMemoryId(), insertResponse_RoomX_MemberO.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertRequest_RoomX_MemberO.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomX_MemberO_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_RoomX_MemberO.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(isNow(deleteMemoryResponseDto.getDeleteDate())).isTrue();
    }
    
    @Test
    @Order(5)
    @Transactional
    void RoomX_MemberX_Memory_Create_Read_Delete() {
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
        
        User Member_IncludeO = userRepo.save(
                User.builder()
                    .snsId("Member_IncludeO_snsId")
                    .snsType(2)
                    .pushToken("Member_IncludeO Token")
                    .name("Member_IncludeO")
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
        mainRoom_Member.add(Member_IncludeO.getId());
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", Member_IncludeO.getId(), false, mainRoom_Member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", Member_IncludeX.getId(), false, mainRoom_Member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertRequest_RoomX_MemberX = new InsertMemoryDto.Request(
                Creator.getId(),
                null,
                "Test Memory",
                null,
                "Test Contents", 
                "Test Place", 
                LocalDateTime.parse("2021-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2021-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2021-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );
        
        /* 1. Make memory */
        InsertMemoryDto.Response insertResponse_RoomX_MemberX = memoryService.insert(insertRequest_RoomX_MemberX);
        assertThat(insertResponse_RoomX_MemberX).isNotNull();
        assertThat(isNow(insertResponse_RoomX_MemberX.getAddDate())).isTrue();
        assertThat(insertResponse_RoomX_MemberX.getRoomId()).isNull();
        
        log.info("[RoomX_MemberX] CreateDate: {} memoryId: {}, roomId: {}", insertResponse_RoomX_MemberX.getAddDate(),
                insertResponse_RoomX_MemberX.getMemoryId(), insertResponse_RoomX_MemberX.getRoomId());
        
        /* 2. Find memory */
        List<Memory> responseList = memoryService.findMemories(insertRequest_RoomX_MemberX.getUserId());
        assertThat(responseList).isNotNull();
        
        log.info("[RoomX_MemberX_Memory_Read]");
        responseList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteMemoryResponseDto = memoryService.deleteMemory(insertResponse_RoomX_MemberX.getMemoryId());
        
        assertThat(deleteMemoryResponseDto).isNotNull();
        assertThat(isNow(deleteMemoryResponseDto.getDeleteDate())).isTrue();
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
