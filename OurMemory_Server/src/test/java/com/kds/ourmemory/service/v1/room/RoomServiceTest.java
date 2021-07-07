package com.kds.ourmemory.service.v1.room;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryDto;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.FindRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.UpdateRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.memory.MemoryService;
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
class RoomServiceTest {
    private final RoomService roomService;
    private final MemoryService memoryService;  // The creation process from adding to the deletion of the memory.

    private final UserRepository userRepo; // Add to work with user data

    /**
     * Assert time format -> delete sec
     * 
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    @Autowired
    private RoomServiceTest(RoomService roomService, MemoryService memoryService, UserRepository userRepo) {
        this.roomService = roomService;
        this.memoryService = memoryService;
        this.userRepo = userRepo;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @Transactional
    void Create_Read_Update_Delete() {
        /* 0-1. Create owner, member */
        User owner = userRepo.save(
                User.builder()
                .snsId("owner_snsId")
                .snsType(1)
                .pushToken("owner Token")
                .name("owner")
                .birthday("0724")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.ANDROID)
                .role(UserRole.USER)
                .build()
        );

        User member1 = userRepo.save(
                User.builder()
                .snsId("member1_snsId")
                .snsType(2)
                .pushToken("member1 Token")
                .name("member1")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.IOS)
                .role(UserRole.USER)
                .build()
        );

        User member2 = userRepo.save(
                User.builder()
                .snsId("member2_snsId")
                .snsType(2)
                .pushToken("member2 Token")
                .name("member2")
                .birthday("0807")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.IOS)
                .role(UserRole.USER)
                .build()
        );

        List<Long> member = new ArrayList<>();
        member.add(member1.getId());
        member.add(member2.getId());

        /* 0-2. Create request */
        InsertRoomDto.Request insertReq = new InsertRoomDto.Request("TestRoom", owner.getId(), false, member);
        UpdateRoomDto.Request updateReq = new UpdateRoomDto.Request("update room name", true);

        /* 1. Insert */
        InsertRoomDto.Response insertRsp = roomService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getOwnerId()).isEqualTo(owner.getId());
        assertThat(insertRsp.getMembers()).isNotNull();
        assertThat(insertRsp.getMembers().size()).isEqualTo(3);

        /* 2. Find rooms */
        List<Room> findRooms = roomService.findRooms(owner.getId(), null);
        assertThat(findRooms).isNotNull();

        findRooms = roomService.findRooms(null, "TestRoom");
        assertThat(findRooms).isNotNull();

        log.info("[Create_Read_Update_Delete] Find rooms");
        findRooms.forEach(room -> log.info(room.toString()));

        /* 3. Find before update */
        FindRoomDto.Response beforeFindRsp = roomService.find(insertRsp.getRoomId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(beforeFindRsp.isOpened()).isEqualTo(insertReq.isOpened());

        /* 4. Update */
        UpdateRoomDto.Response updateRsp = roomService.update(insertRsp.getRoomId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertThat(isNow(updateRsp.getUpdateDate())).isTrue();

        /* 5. Find after update */
        FindRoomDto.Response afterFindRsp = roomService.find(insertRsp.getRoomId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterFindRsp.isOpened()).isEqualTo(updateReq.getOpened());
        
        /* 6. Delete */
        DeleteRoomDto.Response deleteRsp = roomService.delete(insertRsp.getRoomId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 7. Find after delete */
        Long roomId = insertRsp.getRoomId();
        assertThat(roomId).isNotNull();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(roomId)
        );

        log.info("deleteDate: {}", deleteRsp.getDeleteDate());
    }

    @Test
    @Order(2)
    @Transactional
    void deleteRoomAndCheckMemories() {
        /* 0-1. Create owner, member */
        User owner = userRepo.save(
                User.builder()
                        .snsId("owner_snsId")
                        .snsType(1)
                        .pushToken("owner Token")
                        .name("owner")
                        .birthday("0724")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.ANDROID)
                        .role(UserRole.USER)
                        .build()
        );

        User member1 = userRepo.save(
                User.builder()
                        .snsId("member1_snsId")
                        .snsType(2)
                        .pushToken("member1 Token")
                        .name("member1")
                        .birthday("0519")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );

        User member2 = userRepo.save(
                User.builder()
                        .snsId("member2_snsId")
                        .snsType(2)
                        .pushToken("member2 Token")
                        .name("member2")
                        .birthday("0807")
                        .solar(true)
                        .birthdayOpen(true)
                        .used(true)
                        .deviceOs(DeviceOs.IOS)
                        .role(UserRole.USER)
                        .build()
        );

        List<Long> member = new ArrayList<>();
        member.add(member1.getId());
        member.add(member2.getId());

        /* 0-2. Create request */
        InsertRoomDto.Request insertRoomReq = new InsertRoomDto.Request("TestRoom", owner.getId(), false, member);

        /* 1. Insert */
        InsertRoomDto.Response insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(owner.getId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        InsertMemoryDto.Request insertMemoryReqOwner = new InsertMemoryDto.Request(
                owner.getId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                Stream.of(owner.getId()).collect(Collectors.toList()),
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null
        );

        InsertMemoryDto.Response insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner).isNotNull();
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(owner.getId());
        assertThat(insertMemoryRspOwner.getMainRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        InsertMemoryDto.Request insertMemoryReqMember1 = new InsertMemoryDto.Request(
                member1.getId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                Stream.of(owner.getId()).collect(Collectors.toList()),
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null
        );

        InsertMemoryDto.Response insertMemoryRspMember1 = memoryService.insert(insertMemoryReqMember1);
        assertThat(insertMemoryRspMember1).isNotNull();
        assertThat(insertMemoryRspMember1.getWriterId()).isEqualTo(member1.getId());
        assertThat(insertMemoryRspMember1.getMainRoomId()).isEqualTo(insertMemoryReqMember1.getRoomId());

        InsertMemoryDto.Request insertMemoryReqMember2 = new InsertMemoryDto.Request(
                member2.getId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                Stream.of(member1.getId()).collect(Collectors.toList()),
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null
        );

        InsertMemoryDto.Response insertMemoryRspMember2 = memoryService.insert(insertMemoryReqMember2);
        assertThat(insertMemoryRspMember2).isNotNull();
        assertThat(insertMemoryRspMember2.getWriterId()).isEqualTo(member2.getId());
        assertThat(insertMemoryRspMember2.getMainRoomId()).isEqualTo(insertMemoryReqMember2.getRoomId());

        /* 3. Delete room */
        DeleteRoomDto.Response deleteRsp = roomService.delete(insertRoomRsp.getRoomId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 7. Find room and memories after delete */
        Long roomId = insertRoomRsp.getRoomId();
        assertThat(roomId).isNotNull();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(roomId)
        );

        Long memoryOwner = insertMemoryRspOwner.getMemoryId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryOwner)
        );

        Long memoryMember1 = insertMemoryRspMember1.getMemoryId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryMember1)
        );

        Long memoryMember2 = insertMemoryRspMember2.getMemoryId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryMember2)
        );
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
