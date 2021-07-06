package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.controller.v1.memory.dto.*;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


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
     * |    O    |0 < Memory member <= room member|    X    |   not yet. Memory.java -> updateColumn() error
     * |    O    |0 < Memory member != room member|    O    |   not yet.
     * |    O    |      0 == Memory member        |    X    |   not yet.
     * |    X    |      0 < Memory member         |    O    |   not yet.
     * |    X    |               X                |    X    |   not yet.
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
    void RoomO_memberO() {
        /* 0-1. Create writer, member */
        User writer = userRepo.save(
                User.builder()
                        .snsId("writer_snsId")
                        .snsType(1)
                        .pushToken("writer Token")
                        .name("writer")
                        .birthday("0724")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );

        User member = userRepo.save(
                User.builder()
                        .snsId("member_snsId")
                        .snsType(2)
                        .pushToken("member Token")
                        .name("member")
                        .birthday("0519")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );

        User member_IncludeX = userRepo.save(
                User.builder()
                        .snsId("member_IncludeX_snsId")
                        .snsType(2)
                        .pushToken("member_IncludeX Token")
                        .name("member_IncludeX")
                        .birthday("0807")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_member = new ArrayList<>();
        mainRoom_member.add(member.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", writer.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", member.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", member_IncludeX.getId(), false, mainRoom_member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                writer.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                Stream.of(member.getId()).collect(Collectors.toList()),
                "Test Contents",
                "Test Place", 
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간 
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                shareRooms     // 공유할 Room
                );

        UpdateMemoryDto.Request updateReq = new UpdateMemoryDto.Request(
                "Update memory name",
                null,
                "Update contents",
                "Update place",
                LocalDateTime.parse("2021-07-08 17:00", alertTimeFormat),
                LocalDateTime.parse("2021-07-09 17:00", alertTimeFormat),
                null,
                null,
                null
        );
        
        /* 1. Insert */
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getWriterId()).isEqualTo(writer.getId());
        assertThat(insertRsp.getMainRoomId()).isEqualTo(insertReq.getRoomId());
        
        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertRsp.getMemoryId());
        
        log.info("[RoomO_memberO_Memory_Read] Find memories");
        findMemoriesList.forEach(memory -> log.info(memory.toString()));

        /* 3. Find before update */
        FindMemoryDto.Response beforeFindRsp = memoryService.find(insertRsp.getMemoryId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertRsp.getName());
        assertThat(beforeFindRsp.getContents()).isEqualTo(insertRsp.getContents());

        /* 4. Update */
        UpdateMemoryDto.Response updateRsp = memoryService.update(insertRsp.getMemoryId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertThat(isNow(updateRsp.getUpdateDate())).isTrue();

        /* 5. Find after update */
        FindMemoryDto.Response afterFindRsp = memoryService.find(insertRsp.getMemoryId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterFindRsp.getContents()).isEqualTo(updateReq.getContents());

        /* 6. Delete */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 7. Find after delete */
        Long memoryId = insertRsp.getMemoryId();
        assertThat(memoryId).isNotNull();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId)
        );
    }
    
    @Test
    @Order(2)
    @Transactional
    void RoomO_memberO_IncludeX_Memory() {
        /* 0-1. Create writer, member */
        User writer = userRepo.save(
                User.builder()
                        .snsId("writer_snsId")
                        .snsType(1)
                        .pushToken("writer Token")
                        .name("writer")
                        .birthday("0724")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );

        User member = userRepo.save(
                User.builder()
                        .snsId("member_snsId")
                        .snsType(2)
                        .pushToken("member Token")
                        .name("member")
                        .birthday("0519")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );

        User member_IncludeX = userRepo.save(
                User.builder()
                        .snsId("member_IncludeX_snsId")
                        .snsType(2)
                        .pushToken("member_IncludeX Token")
                        .name("member_IncludeX")
                        .birthday("0807")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_member = new ArrayList<>();
        mainRoom_member.add(member.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", writer.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", member.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", member_IncludeX.getId(), false, mainRoom_member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                writer.getId(),
                mainRoom.getRoomId(),
                "Test Memory",
                Stream.of(member_IncludeX.getId()).collect(Collectors.toList()),
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
        assertThat(insertRsp.getWriterId()).isEqualTo(writer.getId());
        assertThat(insertRsp.getMainRoomId()).isNotEqualTo(insertReq.getRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertRsp.getMemoryId());
        
        log.info("[RoomO_memberO_IncludeX_Memory_Read]");
        findMemoriesList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");

        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 4. Find after delete */
        Long memoryId = insertRsp.getMemoryId();
        assertThat(memoryId).isNotNull();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId)
        );
    }
    
    @Test
    @Order(3)
    @Transactional
    void RoomO_memberX_Memory() {
        /* 0-1. Create writer, member */
        User writer = userRepo.save(
                User.builder()
                        .snsId("writer_snsId")
                        .snsType(1)
                        .pushToken("writer Token")
                        .name("writer")
                        .birthday("0724")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );

        User member = userRepo.save(
                User.builder()
                        .snsId("member_snsId")
                        .snsType(2)
                        .pushToken("member Token")
                        .name("member")
                        .birthday("0519")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );

        User member_IncludeX = userRepo.save(
                User.builder()
                        .snsId("member_IncludeX_snsId")
                        .snsType(2)
                        .pushToken("member_IncludeX Token")
                        .name("member_IncludeX")
                        .birthday("0807")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_member = new ArrayList<>();
        mainRoom_member.add(member.getId());
        InsertRoomDto.Response mainRoom = roomService.insert(new InsertRoomDto.Request("mainRoom", writer.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", member.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", member_IncludeX.getId(), false, mainRoom_member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                writer.getId(),
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
        assertThat(insertRsp.getWriterId()).isEqualTo(writer.getId());
        assertThat(insertRsp.getMainRoomId()).isEqualTo(insertRsp.getMainRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertRsp.getMemoryId());
        
        log.info("[RoomO_memberX_Memory_Read]");
        findMemoriesList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 4. Find after delete */
        Long memoryId = insertRsp.getMemoryId();
        assertThat(memoryId).isNotNull();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId)
        );
    }
    
    @Test
    @Order(4)
    @Transactional
    void RoomX_memberO_Memory() {
        /* 0-1. Create writer, member */
        User writer = userRepo.save(
                User.builder()
                        .snsId("writer_snsId")
                        .snsType(1)
                        .pushToken("writer Token")
                        .name("writer")
                        .birthday("0724")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );

        User member = userRepo.save(
                User.builder()
                        .snsId("member_snsId")
                        .snsType(2)
                        .pushToken("member Token")
                        .name("member")
                        .birthday("0519")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );

        User member_IncludeX = userRepo.save(
                User.builder()
                        .snsId("member_IncludeX_snsId")
                        .snsType(2)
                        .pushToken("member_IncludeX Token")
                        .name("member_IncludeX")
                        .birthday("0807")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_member = new ArrayList<>();
        mainRoom_member.add(member.getId());
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", member.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", member_IncludeX.getId(), false, mainRoom_member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                writer.getId(),
                null,
                "Test Memory",
                Stream.of(member.getId()).collect(Collectors.toList()),
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
        assertThat(insertRsp.getWriterId()).isEqualTo(writer.getId());
        assertThat(insertRsp.getMainRoomId()).isNotEqualTo(insertReq.getRoomId());

        /* 2. Find memory */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertRsp.getMemoryId());
        
        log.info("[RoomX_memberO_Memory_Read]");
        findMemoriesList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 4. Find after delete */
        Long memoryId = insertRsp.getMemoryId();
        assertThat(memoryId).isNotNull();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId)
        );
    }
    
    @Test
    @Order(5)
    @Transactional
    void RoomX_memberX_Memory() {
        /* 0-1. Create writer, member */
        User writer = userRepo.save(
                User.builder()
                        .snsId("writer_snsId")
                        .snsType(1)
                        .pushToken("writer Token")
                        .name("writer")
                        .birthday("0724")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );

        User member = userRepo.save(
                User.builder()
                        .snsId("member_snsId")
                        .snsType(2)
                        .pushToken("member Token")
                        .name("member")
                        .birthday("0519")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );

        User member_IncludeX = userRepo.save(
                User.builder()
                        .snsId("member_IncludeX_snsId")
                        .snsType(2)
                        .pushToken("member_IncludeX Token")
                        .name("member_IncludeX")
                        .birthday("0807")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );
        
        /* 0-2. Make main room, share room */
        List<Long> mainRoom_member = new ArrayList<>();
        mainRoom_member.add(member.getId());
        InsertRoomDto.Response shareRoom1 = roomService.insert(new InsertRoomDto.Request("shareRoom1", member.getId(), false, mainRoom_member));
        InsertRoomDto.Response shareRoom2 = roomService.insert(new InsertRoomDto.Request("shareRoom2", member_IncludeX.getId(), false, mainRoom_member));
        
        List<Long> shareRooms = new ArrayList<>();
        shareRooms.add(shareRoom1.getRoomId());
        shareRooms.add(shareRoom2.getRoomId());
        
        /* 0-3. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
                writer.getId(),
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
        assertThat(insertRsp.getWriterId()).isEqualTo(writer.getId());
        assertThat(insertRsp.getMainRoomId()).isNull();

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertRsp.getMemoryId());
        
        log.info("[RoomX_memberX_Memory_Read]");
        findMemoriesList.forEach(memory -> log.info(memory.toString()));
        log.info("====================================================================================");
        
        /* 3. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 4. Find after delete */
        Long memoryId = insertRsp.getMemoryId();
        assertThat(memoryId).isNotNull();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId)
        );
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
