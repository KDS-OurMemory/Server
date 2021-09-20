package com.kds.ourmemory.service.v1.room;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomAlreadyOwnerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundMemberException;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryDto;
import com.kds.ourmemory.controller.v1.memory.dto.ShareMemoryDto;
import com.kds.ourmemory.controller.v1.room.dto.*;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import com.kds.ourmemory.service.v1.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomServiceTest {
    private final RoomService roomService;

    private final MemoryService memoryService;  // The creation process from adding to the deletion of the memory.

    private final UserService userService;  // The creation process from adding to the deletion of the room.

    /**
     * Assert time format -> delete sec
     * 
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    // Base data for test RoomService
    private InsertUserDto.Response insertOwnerRsp;

    private InsertUserDto.Response insertMember1Rsp;
    
    private InsertUserDto.Response insertMember2Rsp;

    private List<Long> roomMembers;

    @Autowired
    private RoomServiceTest(RoomService roomService, MemoryService memoryService, UserService userService) {
        this.roomService = roomService;
        this.memoryService = memoryService;
        this.userService = userService;
    }

    @BeforeAll
    void setUp() {
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @DisplayName("생성-조회-수정-삭제")
    @Transactional
    void crud() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertRoomDto.Request insertRoomReq = new InsertRoomDto.Request(
                "TestRoom", insertOwnerRsp.getUserId(), false, roomMembers);
        UpdateRoomDto.Request updateRoomReq = new UpdateRoomDto.Request("update room name", true);
        DeleteRoomDto.Request deleteRoomReq = new DeleteRoomDto.Request(insertOwnerRsp.getUserId());

        /* 1. Insert */
        InsertRoomDto.Response insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Find rooms */
        List<FindRoomsDto.Response> findRooms = roomService.findRooms(insertOwnerRsp.getUserId(), null);
        assertThat(findRooms).isNotNull();

        findRooms = roomService.findRooms(null, "TestRoom");
        assertThat(findRooms).isNotNull();
        assertThat(findRooms.size()).isOne();

        log.info("[Create_Read_Update_Delete] Find rooms");
        findRooms.forEach(room -> log.info(room.toString()));

        /* 3. Find before update */
        FindRoomDto.Response beforeFindRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(beforeFindRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());

        /* 4. Update */
        UpdateRoomDto.Response updateRsp = roomService.update(insertRoomRsp.getRoomId(), updateRoomReq);
        assertThat(updateRsp).isNotNull();

        /* 5. Find after update */
        FindRoomDto.Response afterFindRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateRoomReq.getName());
        assertThat(afterFindRsp.isOpened()).isEqualTo(updateRoomReq.getOpened());
        
        /* 6. Delete */
        DeleteRoomDto.Response deleteRsp = roomService.delete(insertRoomRsp.getRoomId(), deleteRoomReq);
        assertThat(deleteRsp).isNotNull();

        /* 7. Find after delete */
        Long roomId = insertRoomRsp.getRoomId();
        assertThat(roomId).isNotNull();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(roomId)
        );
    }

    @Test
    @Order(2)
    @DisplayName("방 개별 조회")
    @Transactional
    void findRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = new InsertRoomDto.Request(
                "TestRoom", insertOwnerRsp.getUserId(), false,
                Stream.of(insertMember1Rsp.getUserId()).collect(toList())
        );

        var insertShareRoomReq = new InsertRoomDto.Request(
                "TestRoom", insertOwnerRsp.getUserId(), false,
                Stream.of(insertMember2Rsp.getUserId()).collect(toList())
        );

        /* 1. Insert room, share room */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(2);

        var insertShareRoomRsp = roomService.insert(insertShareRoomReq);
        assertThat(insertShareRoomRsp).isNotNull();
        assertThat(insertShareRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertShareRoomRsp.getMembers()).isNotNull();
        assertThat(insertShareRoomRsp.getMembers().size()).isEqualTo(2);

        /* 2. Insert memory */
        var insertMemoryReq = new InsertMemoryDto.Request(
                insertOwnerRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 3. Share Memory */
        var shareMemoryRsp = memoryService.shareMemory(
                insertMemoryRsp.getMemoryId(), insertOwnerRsp.getUserId(),
                new ShareMemoryDto.Request(
                        ShareMemoryDto.ShareType.ROOMS,
                        Stream.of(insertShareRoomRsp.getRoomId()).collect(toList())
                )
        );
        assertThat(shareMemoryRsp).isNotNull();

        /* 4. Set attendance */
        var setAttendanceOwnerRsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(),
                insertOwnerRsp.getUserId(),
                AttendanceStatus.ATTEND
        );
        assertThat(setAttendanceOwnerRsp).isNotNull();

        var setAttendanceMember1Rsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(),
                insertMember1Rsp.getUserId(),
                AttendanceStatus.ABSENCE
        );
        assertThat(setAttendanceMember1Rsp).isNotNull();

        var setAttendanceMember2Rsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(),
                insertMember2Rsp.getUserId(),
                AttendanceStatus.ATTEND
        );
        assertThat(setAttendanceMember2Rsp).isNotNull();

        /* 5. Find room */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp).isNotNull();
        log.debug("==========================================================");
        log.debug(findRoomRsp.toString());
        log.debug("==========================================================");
    }

    @Test
    @Order(3)
    @DisplayName("방 삭제 -> 공유방")
    @Transactional
    void deleteShareRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertRoomDto.Request insertRoomReq = new InsertRoomDto.Request(
                "TestRoom", insertOwnerRsp.getUserId(), false, roomMembers);
        DeleteRoomDto.Request deleteRoomReq = new DeleteRoomDto.Request(insertOwnerRsp.getUserId());

        /* 1. Insert */
        InsertRoomDto.Response insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        InsertMemoryDto.Request insertMemoryReqOwner = new InsertMemoryDto.Request(
                insertOwnerRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        InsertMemoryDto.Response insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        InsertMemoryDto.Request insertMemoryReqMember1 = new InsertMemoryDto.Request(
                insertMember1Rsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,   // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        InsertMemoryDto.Response insertMemoryRspMember1 = memoryService.insert(insertMemoryReqMember1);
        assertThat(insertMemoryRspMember1.getWriterId()).isEqualTo(insertMember1Rsp.getUserId());
        assertThat(insertMemoryRspMember1.getAddedRoomId()).isEqualTo(insertMemoryReqMember1.getRoomId());

        InsertMemoryDto.Request insertMemoryReqMember2 = new InsertMemoryDto.Request(
                insertMember2Rsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,   // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        InsertMemoryDto.Response insertMemoryRspMember2 = memoryService.insert(insertMemoryReqMember2);
        assertThat(insertMemoryRspMember2.getWriterId()).isEqualTo(insertMember2Rsp.getUserId());
        assertThat(insertMemoryRspMember2.getAddedRoomId()).isEqualTo(insertMemoryReqMember2.getRoomId());

        /* 3. Delete share room */
        DeleteRoomDto.Response deleteRsp = roomService.delete(insertRoomRsp.getRoomId(), deleteRoomReq);
        assertThat(deleteRsp).isNotNull();

        /* 4. Find room and memories after delete */
        Long roomId = insertRoomRsp.getRoomId();
        assertThat(roomId).isNotNull();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(roomId)
        );

        // 4-1. Owner
        var ownerMemoryId = insertMemoryRspOwner.getMemoryId();
        var ownerShareRoomId = insertMemoryRspOwner.getAddedRoomId();
        var ownerPrivateRoomId = insertOwnerRsp.getPrivateRoomId();
        // 1) Check delete memory from share room
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.find(ownerMemoryId, ownerShareRoomId)
        );

        // 2) Check not delete memory
        var afterOwnerFindMemoryRsp = memoryService.find(ownerMemoryId, ownerPrivateRoomId);
        assertThat(afterOwnerFindMemoryRsp).isNotNull();

        // 3) Check memory exists private room
        var afterOwnerFindPrivateRoomRsp = roomService.find(insertOwnerRsp.getPrivateRoomId());
        assertThat(afterOwnerFindPrivateRoomRsp.getMemories().size()).isOne();


        // 4-2. Member1
        var member1MemoryId = insertMemoryRspMember1.getMemoryId();
        var member1ShareRoomId = insertMemoryRspMember1.getAddedRoomId();
        var member1PrivateRoomId = insertMember1Rsp.getPrivateRoomId();
        // 1) Check delete memory from share room
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.find(member1MemoryId, member1ShareRoomId)
        );

        // 2) Check not delete memory
        var afterMember1FindMemoryRsp = memoryService.find(member1MemoryId, member1PrivateRoomId);
        assertThat(afterMember1FindMemoryRsp).isNotNull();

        // 3) Check memory exists private room
        var afterMember1FindPrivateRoomRsp = roomService.find(insertMember1Rsp.getPrivateRoomId());
        assertThat(afterMember1FindPrivateRoomRsp.getMemories().size()).isOne();


        // 4-3. Member2
        var member2MemoryId = insertMemoryRspMember2.getMemoryId();
        var memberShare2RoomId = insertMemoryRspMember2.getAddedRoomId();
        var member2PrivateRoomId = insertMember2Rsp.getPrivateRoomId();
        // 1) Check delete memory from share room
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.find(member2MemoryId, memberShare2RoomId)
        );

        // 2) Check not delete memory
        var afterMember2FindMemoryRsp = memoryService.find(member2MemoryId, member2PrivateRoomId);
        assertThat(afterMember2FindMemoryRsp).isNotNull();

        // 3) Check memory exists private room
        var afterMember2FindPrivateRoomRsp = roomService.find(insertMember2Rsp.getPrivateRoomId());
        assertThat(afterMember2FindPrivateRoomRsp.getMemories().size()).isOne();
    }

    @Test
    @Order(4)
    @DisplayName("방 삭제 -> 개인방")
    @Transactional
    void deletePrivateRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = new InsertRoomDto.Request(
                "TestRoom", insertOwnerRsp.getUserId(), false, roomMembers);
        var deleteRoomReq = new DeleteRoomDto.Request(insertOwnerRsp.getUserId());

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        var insertMemoryReqOwner = new InsertMemoryDto.Request(
                insertOwnerRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                null,
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        var insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner).isNotNull();
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        /* 3. Delete room */
        var deleteRsp = roomService.delete(insertOwnerRsp.getPrivateRoomId(), deleteRoomReq);
        assertThat(deleteRsp).isNotNull();

        /* 4. Find room and memories after delete */
        Long privateRoomId = insertOwnerRsp.getPrivateRoomId();
        assertThat(privateRoomId).isNotNull();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(privateRoomId)
        );

        var afterFindMemoriesList = memoryService.findMemories(insertOwnerRsp.getUserId(), null);
        assertTrue(afterFindMemoriesList.isEmpty());

        var memoryOwner = insertMemoryRspOwner.getMemoryId();
        var roomIdOwner = insertMemoryRspOwner.getAddedRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryOwner, roomIdOwner)
        );
    }

    @Test
    @Order(5)
    @DisplayName("방장 양도")
    @Transactional
    void patchOwner() {
        /* 0-1. Set base data */
        setBaseData();

        var insertExcludeMemberReq = new InsertUserDto.Request(
                1, "excludeMember_snsId", "excludeMember Token",
                "excludeMember", "1225", true,
                false, DeviceOs.IOS
        );
        var insertExcludeMemberRsp = userService.signUp(insertExcludeMemberReq);
        assertThat(insertExcludeMemberRsp).isNotNull();
        assertThat(insertExcludeMemberRsp.getUserId()).isNotNull();
        assertThat(insertExcludeMemberRsp.getPrivateRoomId()).isNotNull();

        /* 0-2. Create request */
        InsertRoomDto.Request insertRoomReq = new InsertRoomDto.Request(
                "TestRoom", insertOwnerRsp.getUserId(), false, roomMembers);

        /* 1. Insert */
        InsertRoomDto.Response insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);
        
        /* 2. Find before patch owner */
        FindRoomDto.Response beforeFindRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());

        /* 3. Patch owner */
        PatchRoomOwnerDto.Response patchOwnerRsp = roomService.patchOwner(insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId());
        assertThat(patchOwnerRsp).isNotNull();

        /* 4. Find after patch owner */
        FindRoomDto.Response afterFindRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getOwnerId()).isEqualTo(insertMember1Rsp.getUserId());

        /* 5. Patch owner not found room */
        var wrongRoomId = insertRoomRsp.getRoomId() + 1;
        var memberId = insertMember1Rsp.getUserId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.patchOwner(wrongRoomId, memberId)
        );

        /* 6. Patch owner not in room member */
        var roomId = insertRoomRsp.getRoomId();
        var excludeMemberId = insertExcludeMemberRsp.getUserId();
        assertThrows(
                RoomNotFoundMemberException.class, () -> roomService.patchOwner(roomId, excludeMemberId)
        );

        /* 7. Patch owner already owner */
        var ownerId = afterFindRsp.getOwnerId();
        assertThrows(
                RoomAlreadyOwnerException.class, () -> roomService.patchOwner(roomId, ownerId)
        );
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create Owner, Member1, Member2 */
        var insertOwnerReq = new InsertUserDto.Request(
                1, "writer_snsId", "member Token",
                "member", "0519", true,
                false, DeviceOs.IOS
        );
        insertOwnerRsp = userService.signUp(insertOwnerReq);
        assertThat(insertOwnerRsp).isNotNull();
        assertThat(insertOwnerRsp.getUserId()).isNotNull();
        assertThat(insertOwnerRsp.getPrivateRoomId()).isNotNull();

        var insertMember1Req = new InsertUserDto.Request(
                1, "member1_snsId", "member1 Token",
                "member1", "0720", true,
                false, DeviceOs.ANDROID
        );
        insertMember1Rsp = userService.signUp(insertMember1Req);
        assertThat(insertMember1Rsp).isNotNull();
        assertThat(insertMember1Rsp.getUserId()).isNotNull();
        assertThat(insertMember1Rsp.getPrivateRoomId()).isNotNull();

        var insertMember2Req = new InsertUserDto.Request(
                1, "member2_snsId", "member2 Token",
                "member2", "0827", true,
                false, DeviceOs.IOS
        );
        insertMember2Rsp = userService.signUp(insertMember2Req);
        assertThat(insertMember2Rsp).isNotNull();
        assertThat(insertMember2Rsp.getUserId()).isNotNull();
        assertThat(insertMember2Rsp.getPrivateRoomId()).isNotNull();

        roomMembers = new ArrayList<>();
        roomMembers.add(insertMember1Rsp.getUserId());
        roomMembers.add(insertMember2Rsp.getUserId());
    }
}
