package com.kds.ourmemory.room.v1.service;

import com.kds.ourmemory.room.v1.advice.exception.*;
import com.kds.ourmemory.memory.v1.advice.exception.MemoryNotFoundException;
import com.kds.ourmemory.user.v1.advice.exception.UserNotFoundException;
import com.kds.ourmemory.memory.v1.controller.dto.MemoryReqDto;
import com.kds.ourmemory.room.v1.controller.dto.RoomReqDto;
import com.kds.ourmemory.room.v1.controller.dto.RoomRspDto;
import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import com.kds.ourmemory.user.v1.controller.dto.UserRspDto;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.memory.v1.service.MemoryService;
import com.kds.ourmemory.user.v1.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomServiceTest {

    private final RoomService roomService;

    private final MemoryService memoryService;  // The creation process from adding to the deletion of the memory.

    private final UserService userService;  // The creation process from adding to the deletion of the user.

    /**
     * Assert time format -> delete sec
     * <p>
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    // Base data for test RoomService
    private UserRspDto insertOwnerRsp;

    private UserRspDto insertMember1Rsp;

    private UserRspDto insertMember2Rsp;

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
    @DisplayName("생성 | 성공")
    void insertSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);
    }

    @Test
    @DisplayName("생성 | 실패 | 사용자번호 다름")
    void insertFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId() + 10000)
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        assertThrows(
                RoomNotFoundOwnerException.class, () -> roomService.insert(insertRoomReq)
        );
    }

    @Test
    @DisplayName("방 개별 조회 | 성공")
    void findSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(List.of(insertMember1Rsp.getUserId()))
                .build();

        /* 1. Insert room and share room */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Find room */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getRoomId()).isEqualTo(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getName()).isEqualTo(insertRoomRsp.getName());
    }

    @Test
    @DisplayName("방 개별 조회 | 실패 | 방 번호 다름")
    void findFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(List.of(insertMember1Rsp.getUserId()))
                .build();

        /* 1. Insert room and share room */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Find room */
        var wrongRoomId = insertRoomRsp.getRoomId() + 1;
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(wrongRoomId)
        );
    }

    @Test
    @DisplayName("방 목록 조회 - 방장번호 | 성공")
    void findsSuccessToUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertOwnerRoomReq1 = RoomReqDto.builder()
                .name("ownerRoom1")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var insertOwnerRoomReq2 = RoomReqDto.builder()
                .name("ownerRoom2")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var insertParticipantRoomReq1 = RoomReqDto.builder()
                .name("participantRoom1")
                .userId(insertMember1Rsp.getUserId())
                .opened(false)
                .member(List.of(insertOwnerRsp.getUserId()))
                .build();

        var insertParticipantRoomReq2 = RoomReqDto.builder()
                .name("participantRoom2")
                .userId(insertMember2Rsp.getUserId())
                .opened(true)
                .member(List.of(insertOwnerRsp.getUserId()))
                .build();

        /* 1. Insert */
        var insertOwnerRoomRsp2 = roomService.insert(insertOwnerRoomReq2);
        assertThat(insertOwnerRoomRsp2.getOwnerId()).isEqualTo(insertOwnerRoomReq2.getUserId());
        assertThat(insertOwnerRoomRsp2.getName()).isEqualTo(insertOwnerRoomReq2.getName());
        assertThat(insertOwnerRoomRsp2.isOpened()).isEqualTo(insertOwnerRoomReq2.isOpened());
        assertMembers(insertOwnerRoomRsp2, insertOwnerRoomReq2);

        var insertOwnerRoomRsp1 = roomService.insert(insertOwnerRoomReq1);
        assertThat(insertOwnerRoomRsp1.getOwnerId()).isEqualTo(insertOwnerRoomReq1.getUserId());
        assertThat(insertOwnerRoomRsp1.getName()).isEqualTo(insertOwnerRoomReq1.getName());
        assertThat(insertOwnerRoomRsp1.isOpened()).isEqualTo(insertOwnerRoomReq1.isOpened());
        assertMembers(insertOwnerRoomRsp1, insertOwnerRoomReq1);

        var insertParticipantRoomRsp1 = roomService.insert(insertParticipantRoomReq1);
        assertThat(insertParticipantRoomRsp1.getOwnerId()).isEqualTo(insertParticipantRoomReq1.getUserId());
        assertThat(insertParticipantRoomRsp1.getName()).isEqualTo(insertParticipantRoomReq1.getName());
        assertThat(insertParticipantRoomRsp1.isOpened()).isEqualTo(insertParticipantRoomReq1.isOpened());
        assertMembers(insertParticipantRoomRsp1, insertParticipantRoomReq1);

        var insertParticipantRoomRsp2 = roomService.insert(insertParticipantRoomReq2);
        assertThat(insertParticipantRoomRsp2.getOwnerId()).isEqualTo(insertParticipantRoomReq2.getUserId());
        assertThat(insertParticipantRoomRsp2.getName()).isEqualTo(insertParticipantRoomReq2.getName());
        assertThat(insertParticipantRoomRsp2.isOpened()).isEqualTo(insertParticipantRoomReq2.isOpened());
        assertMembers(insertParticipantRoomRsp2, insertParticipantRoomReq2);

        /* 2. Find rooms and check private room not found */
        var findRoomsRsp = roomService.findRooms(insertOwnerRsp.getUserId(), null);
        assertThat(findRoomsRsp).isNotNull();
        assertThat(findRoomsRsp.size()).isEqualTo(4);

        // Expect Order: ParticipantRoom2 -> ParticipantRoom1 -> OwnerRoom1 -> OwnerRoom2 (Reverse Sort)
        // only id check -> Because all value already checked from step 1. insert
        var findRoomRsp1 = findRoomsRsp.get(0);
        assertThat(findRoomRsp1.getRoomId()).isEqualTo(insertParticipantRoomRsp2.getRoomId());

        var findRoomRsp2 = findRoomsRsp.get(1);
        assertThat(findRoomRsp2.getRoomId()).isEqualTo(insertParticipantRoomRsp1.getRoomId());

        var findRoomRsp3 = findRoomsRsp.get(2);
        assertThat(findRoomRsp3.getRoomId()).isEqualTo(insertOwnerRoomRsp1.getRoomId());

        var findRoomRsp4 = findRoomsRsp.get(3);
        assertThat(findRoomRsp4.getRoomId()).isEqualTo(insertOwnerRoomRsp2.getRoomId());
    }

    @Test
    @DisplayName("방 목록 조회 - 방이름 | 성공")
    void findsSuccessToRoomName() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var sameRoomName = "isSameRoom!";
        var insertOwnerRoomReq1 = RoomReqDto.builder()
                .name(sameRoomName)
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var insertOwnerRoomReq2 = RoomReqDto.builder()
                .name("ownerRoom1")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var insertParticipantRoomReq1 = RoomReqDto.builder()
                .name("participantRoom1")
                .userId(insertMember1Rsp.getUserId())
                .opened(false)
                .member(List.of(insertOwnerRsp.getUserId()))
                .build();

        var insertParticipantRoomReq2 = RoomReqDto.builder()
                .name(sameRoomName)
                .userId(insertMember2Rsp.getUserId())
                .opened(true)
                .member(List.of(insertOwnerRsp.getUserId()))
                .build();

        /* 1. Insert */
        var insertOwnerRoomRsp2 = roomService.insert(insertOwnerRoomReq2);
        assertThat(insertOwnerRoomRsp2.getOwnerId()).isEqualTo(insertOwnerRoomReq2.getUserId());
        assertThat(insertOwnerRoomRsp2.getName()).isEqualTo(insertOwnerRoomReq2.getName());
        assertThat(insertOwnerRoomRsp2.isOpened()).isEqualTo(insertOwnerRoomReq2.isOpened());
        assertMembers(insertOwnerRoomRsp2, insertOwnerRoomReq2);

        var insertOwnerRoomRsp1 = roomService.insert(insertOwnerRoomReq1);
        assertThat(insertOwnerRoomRsp1.getOwnerId()).isEqualTo(insertOwnerRoomReq1.getUserId());
        assertThat(insertOwnerRoomRsp1.getName()).isEqualTo(insertOwnerRoomReq1.getName());
        assertThat(insertOwnerRoomRsp1.isOpened()).isEqualTo(insertOwnerRoomReq1.isOpened());
        assertMembers(insertOwnerRoomRsp1, insertOwnerRoomReq1);

        var insertParticipantRoomRsp1 = roomService.insert(insertParticipantRoomReq1);
        assertThat(insertParticipantRoomRsp1.getOwnerId()).isEqualTo(insertParticipantRoomReq1.getUserId());
        assertThat(insertParticipantRoomRsp1.getName()).isEqualTo(insertParticipantRoomReq1.getName());
        assertThat(insertParticipantRoomRsp1.isOpened()).isEqualTo(insertParticipantRoomReq1.isOpened());
        assertMembers(insertParticipantRoomRsp1, insertParticipantRoomReq1);

        var insertParticipantRoomRsp2 = roomService.insert(insertParticipantRoomReq2);
        assertThat(insertParticipantRoomRsp2.getOwnerId()).isEqualTo(insertParticipantRoomReq2.getUserId());
        assertThat(insertParticipantRoomRsp2.getName()).isEqualTo(insertParticipantRoomReq2.getName());
        assertThat(insertParticipantRoomRsp2.isOpened()).isEqualTo(insertParticipantRoomReq2.isOpened());
        assertMembers(insertParticipantRoomRsp2, insertParticipantRoomReq2);

        /* 2. Find rooms and check private room not found */
        var findRoomsRsp = roomService.findRooms(null, sameRoomName);
        assertThat(findRoomsRsp).isNotNull();
        assertThat(findRoomsRsp.size()).isEqualTo(2);

        // Expect Order: ParticipantRoom2 -> OwnerRoom1 (Reverse Sort)
        // only id check -> Because all value already checked from step 1. insert
        var findRoomRsp1 = findRoomsRsp.get(0);
        assertThat(findRoomRsp1.getRoomId()).isEqualTo(insertParticipantRoomRsp2.getRoomId());

        var findRoomRsp2 = findRoomsRsp.get(1);
        assertThat(findRoomRsp2.getRoomId()).isEqualTo(insertOwnerRoomRsp1.getRoomId());
    }

    @Test
    @DisplayName("방장 양도 | 성공")
    void recommendOwnerSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        var insertExcludeMemberReq = UserReqDto.builder()
                .snsType(1)
                .snsId("excludeMember_snsId")
                .pushToken("excludeMember Token")
                .push(true)
                .name("excludeMember")
                .birthday("1225")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        var insertExcludeMemberRsp = userService.signUp(insertExcludeMemberReq);
        assertThat(insertExcludeMemberRsp).isNotNull();
        assertThat(insertExcludeMemberRsp.getUserId()).isNotNull();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Patch owner */
        var recommendOwnerRsp = roomService.recommendOwner(insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId());
        assertThat(recommendOwnerRsp).isNotNull();
        assertThat(recommendOwnerRsp.getOwnerId()).isEqualTo(insertMember1Rsp.getUserId());

        /* 3. Patch owner not found room */
        var wrongRoomId = insertRoomRsp.getRoomId() + 1;
        var memberId = insertMember1Rsp.getUserId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.recommendOwner(wrongRoomId, memberId)
        );

        /* 4. Patch owner not in room member */
        var roomId = insertRoomRsp.getRoomId();
        var excludeMemberId = insertExcludeMemberRsp.getUserId();
        assertThrows(
                RoomNotFoundMemberException.class, () -> roomService.recommendOwner(roomId, excludeMemberId)
        );

        /* 5. Patch owner already owner */
        var ownerId = recommendOwnerRsp.getOwnerId();
        assertThrows(
                RoomAlreadyOwnerException.class, () -> roomService.recommendOwner(roomId, ownerId)
        );
    }

    @Test
    @DisplayName("방장 양도 | 실패 | 방 번호에 맞는 방이 없는 경우")
    void recommendOwnerFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        var insertExcludeMemberReq = UserReqDto.builder()
                .snsType(1)
                .snsId("excludeMember_snsId")
                .pushToken("excludeMember Token")
                .push(true)
                .name("excludeMember")
                .birthday("1225")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        var insertExcludeMemberRsp = userService.signUp(insertExcludeMemberReq);
        assertThat(insertExcludeMemberRsp).isNotNull();
        assertThat(insertExcludeMemberRsp.getUserId()).isNotNull();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Patch owner not found room */
        var wrongRoomId = insertRoomRsp.getRoomId() + 1;
        var memberId = insertMember1Rsp.getUserId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.recommendOwner(wrongRoomId, memberId)
        );
    }

    @Test
    @DisplayName("방장 양도 | 실패 | 양도할 사용자가 방에 없는 경우")
    void recommendOwnerFailToNotInRoomMember() {
        /* 0-1. Set base data */
        setBaseData();

        var insertExcludeMemberReq = UserReqDto.builder()
                .snsType(1)
                .snsId("excludeMember_snsId")
                .pushToken("excludeMember Token")
                .push(true)
                .name("excludeMember")
                .birthday("1225")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        var insertExcludeMemberRsp = userService.signUp(insertExcludeMemberReq);
        assertThat(insertExcludeMemberRsp).isNotNull();
        assertThat(insertExcludeMemberRsp.getUserId()).isNotNull();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Patch owner */
        var recommendOwnerRsp = roomService.recommendOwner(insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId());
        assertThat(recommendOwnerRsp).isNotNull();
        assertThat(recommendOwnerRsp.getOwnerId()).isEqualTo(insertMember1Rsp.getUserId());

        /* 3. Patch owner not in room member */
        var roomId = insertRoomRsp.getRoomId();
        var excludeMemberId = insertExcludeMemberRsp.getUserId();
        assertThrows(
                RoomNotFoundMemberException.class, () -> roomService.recommendOwner(roomId, excludeMemberId)
        );
    }

    @Test
    @DisplayName("방장 양도 | 실패 | 양도할 사용자가 이미 방장인 경우")
    void recommendOwnerFailToAlreadyOwner() {
        /* 0-1. Set base data */
        setBaseData();

        var insertExcludeMemberReq = UserReqDto.builder()
                .snsType(1)
                .snsId("excludeMember_snsId")
                .pushToken("excludeMember Token")
                .push(true)
                .name("excludeMember")
                .birthday("1225")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        var insertExcludeMemberRsp = userService.signUp(insertExcludeMemberReq);
        assertThat(insertExcludeMemberRsp).isNotNull();
        assertThat(insertExcludeMemberRsp.getUserId()).isNotNull();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Patch owner */
        var recommendOwnerRsp = roomService.recommendOwner(insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId());
        assertThat(recommendOwnerRsp).isNotNull();
        assertThat(recommendOwnerRsp.getOwnerId()).isEqualTo(insertMember1Rsp.getUserId());

        /* 3. Patch owner already owner */
        var roomId = insertRoomRsp.getRoomId();
        var ownerId = recommendOwnerRsp.getOwnerId();
        assertThrows(
                RoomAlreadyOwnerException.class, () -> roomService.recommendOwner(roomId, ownerId)
        );
    }

    @Test
    @DisplayName("수정 | 성공")
    void updateSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var updateRoomReq = RoomReqDto.builder()
                .name("update room name")
                .opened(false)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Update */
        var updateRsp = roomService.update(insertRoomRsp.getRoomId(), updateRoomReq);
        assertThat(updateRsp).isNotNull();
        assertThat(updateRsp.getName()).isEqualTo(updateRoomReq.getName());
        assertThat(updateRsp.isOpened()).isEqualTo(updateRoomReq.isOpened());
    }

    @Test
    @DisplayName("수정 | 실패 | 방번호 다름")
    void updateFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var updateRoomReq = RoomReqDto.builder()
                .name("update room name")
                .opened(false)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Update */
        var wrongRoomId = insertRoomRsp.getRoomId() + 1;
        assertThrows(
                RoomNotFoundException.class, () -> roomService.update(wrongRoomId, updateRoomReq)
        );
    }

    @Test
    @DisplayName("방 삭제 -> 공유방 | 성공")
    void deleteShareRoomSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        var insertMemoryReqOwner = MemoryReqDto.builder()
                .userId(insertOwnerRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        var insertMemoryReqMember1 = MemoryReqDto.builder()
                .userId(insertMember1Rsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryRspMember1 = memoryService.insert(insertMemoryReqMember1);
        assertThat(insertMemoryRspMember1.getWriterId()).isEqualTo(insertMember1Rsp.getUserId());
        assertThat(insertMemoryRspMember1.getAddedRoomId()).isEqualTo(insertMemoryReqMember1.getRoomId());

        var insertMemoryReqMember2 = MemoryReqDto.builder()
                .userId(insertMember2Rsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryRspMember2 = memoryService.insert(insertMemoryReqMember2);
        assertThat(insertMemoryRspMember2.getWriterId()).isEqualTo(insertMember2Rsp.getUserId());
        assertThat(insertMemoryRspMember2.getAddedRoomId()).isEqualTo(insertMemoryReqMember2.getRoomId());

        /* 3. Delete share room */
        var deleteRsp = roomService.delete(insertRoomRsp.getRoomId(), insertOwnerRsp.getUserId());
        assertNull(deleteRsp);

        /* 4. Find room and memories after delete */
        var roomId = insertRoomRsp.getRoomId();
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
    @DisplayName("방 삭제 -> 개인방 | 성공")
    void deletePrivateRoomSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        var insertMemoryReqOwner = MemoryReqDto.builder()
                .userId(insertOwnerRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner).isNotNull();
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        /* 3. Delete room */
        var deleteRsp = roomService.delete(insertOwnerRsp.getPrivateRoomId(), insertOwnerRsp.getUserId());
        assertNull(deleteRsp);

        /* 4. Find room and memories after delete */
        Long privateRoomId = insertOwnerRsp.getPrivateRoomId();
        assertThat(privateRoomId).isNotNull();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(privateRoomId)
        );

        var afterFindMemoriesList = memoryService.findMemories(insertOwnerRsp.getUserId(), null, null, null);
        assertTrue(afterFindMemoriesList.isEmpty());

        var memoryOwner = insertMemoryRspOwner.getMemoryId();
        var roomIdOwner = insertMemoryRspOwner.getAddedRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryOwner, roomIdOwner)
        );
    }

    @Test
    @DisplayName("방 삭제 | 실패 | 사용자번호 다름")
    void deleteFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        var insertMemoryReqOwner = MemoryReqDto.builder()
                .userId(insertOwnerRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner).isNotNull();
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        /* 3. Delete room */
        var wrongRoomId = insertOwnerRsp.getPrivateRoomId() + 10000;
        var userId = insertOwnerRsp.getUserId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.delete(wrongRoomId, userId)
        );
    }

    @Test
    @DisplayName("방 삭제 | 실패 | 방번호 다름")
    void deleteFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        var insertMemoryReqOwner = MemoryReqDto.builder()
                .userId(insertOwnerRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner).isNotNull();
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        /* 3. Delete room */
        var roomId = insertOwnerRsp.getPrivateRoomId();
        var wrongUserId = insertOwnerRsp.getUserId() + 10000;
        assertThrows(
                UserNotFoundException.class, () -> roomService.delete(roomId, wrongUserId)
        );
    }

    @Test
    @DisplayName("방 삭제 | 실패 | 방 소유자가 아님")
    void deleteFailToNotOwner() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        var insertMemoryReqOwner = MemoryReqDto.builder()
                .userId(insertOwnerRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryRspOwner = memoryService.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner).isNotNull();
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        /* 3. Delete room */
        var roomId = insertRoomRsp.getRoomId();
        var notOwnerUserId = insertMember2Rsp.getUserId();
        assertThrows(
                RoomNotOwnerException.class, () -> roomService.delete(roomId, notOwnerUserId)
        );
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 성공 | 방장 지정")
    void exitShareRoomOwnerSuccessToRecommendUser() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var exitRoomRsp = roomService.exit(
                insertRoomRsp.getRoomId(), insertRoomRsp.getOwnerId(), insertMember1Rsp.getUserId()
        );
        assertNull(exitRoomRsp);

        /* 3. Check recommended owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertMember1Rsp.getUserId());
        assertThat(findRoomRsp.getMembers().size()).isEqualTo(2);

        for (var member : findRoomRsp.getMembers()) {
            assertNotEquals(member.getFriendId(), insertRoomRsp.getOwnerId());
        }

        /* 4. Check delete relation from user */
        var findUserRsp = roomService.findRooms(insertRoomRsp.getOwnerId(), null);
        assertThat(findUserRsp.size()).isZero();
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 성공 | 임의 위임")
    void exitShareRoomOwnerSuccessToRandomRecommendUser() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var exitRoomRsp = roomService.exit(insertRoomRsp.getRoomId(), insertRoomRsp.getOwnerId(), null);
        assertNull(exitRoomRsp);

        /* 3. Check recommend owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertNotEquals(findRoomRsp.getOwnerId(), insertRoomRsp.getOwnerId());
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 실패 | 잘못된 방번호")
    void exitShareRoomOwnerFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 1. Exit room */
        var wrongRoomId = -5000L;
        var ownerId = insertOwnerRsp.getUserId();
        var recommendUserId = insertMember1Rsp.getUserId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.exit(wrongRoomId, ownerId, recommendUserId)
        );
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 실패 | 잘못된 사용자번호")
    void exitShareRoomOwnerFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var roomId = insertRoomRsp.getRoomId();
        var wrongOwnerId = -5000L;
        var recommendUserId = insertMember1Rsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> roomService.exit(roomId, wrongOwnerId, recommendUserId)
        );
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 실패 | 참여자가 아닌 사용자번호")
    void exitShareRoomOwnerFailToNotParticipantUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(List.of(insertMember1Rsp.getUserId()))
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var roomId = insertRoomRsp.getRoomId();
        var NotParticipantOwnerId = insertMember2Rsp.getUserId();
        var recommendUserId = insertMember1Rsp.getUserId();
        assertThrows(
                RoomNotParticipantException.class,
                () -> roomService.exit(roomId, NotParticipantOwnerId, recommendUserId)
        );
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 실패 | 잘못된 위임자번호")
    void exitShareRoomOwnerFailToWrongRecommendUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var roomId = insertRoomRsp.getRoomId();
        var ownerId = insertRoomRsp.getOwnerId();
        var wrongRecommendUserId = -5000L;
        assertThrows(
                RoomNotFoundRecommendUserException.class, () -> roomService.exit(roomId, ownerId, wrongRecommendUserId)
        );
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 실패 | 참여자가 아닌 위임자번호")
    void exitShareRoomOwnerFailToNotParticipantRecommendUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(List.of(insertMember1Rsp.getUserId()))
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var roomId = insertRoomRsp.getRoomId();
        var ownerId = insertRoomRsp.getOwnerId();
        var NotParticipantRecommendUserId = insertMember2Rsp.getUserId();
        assertThrows(
                RoomNotParticipantException.class,
                () -> roomService.exit(roomId, ownerId, NotParticipantRecommendUserId)
        );
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 방장인 경우 | 실패 | 위임자가 이미 방장인 경우")
    void exitShareRoomOwnerFailToRecommendUserAlreadyOwner() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(List.of(insertMember1Rsp.getUserId()))
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var roomId = insertRoomRsp.getRoomId();
        var ownerId = insertRoomRsp.getOwnerId();
        var alreadyOwnerId = insertRoomRsp.getOwnerId();
        assertThrows(RoomAlreadyOwnerException.class, () -> roomService.exit(roomId, ownerId, alreadyOwnerId));
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 참여자인 경우 | 성공 | 방장 위임")
    void exitShareRoomParticipantSuccessToRecommendUser() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var exitRoomRsp = roomService.exit(
                insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId(), insertMember2Rsp.getUserId()
        );
        assertNull(exitRoomRsp);

        /* 3. Check same owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 참여자인 경우 | 성공 | 잘못된 위임자번호")
    void exitShareRoomParticipantSuccessToWrongRecommendUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var exitRoomRsp = roomService.exit(
                insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId(), -5000L
        );
        assertNull(exitRoomRsp);

        /* 3. Check same owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 참여자인 경우 | 성공 | 위임자가 이미 방장인 경우")
    void exitShareRoomParticipantSuccessToRecommendUserAlreadyOwner() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var exitRoomRsp = roomService.exit(
                insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId(), insertOwnerRsp.getUserId()
        );
        assertNull(exitRoomRsp);

        /* 3. Check same owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 참여자인 경우 | 성공 | 임의위임")
    void exitShareRoomParticipantSuccessToRandomRecommendUser() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var exitRoomRsp = roomService.exit(
                insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId(), null
        );
        assertNull(exitRoomRsp);

        /* 3. Check same owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 참여자인 경우 | 실패 | 잘못된 방번호")
    void exitShareRoomParticipantFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var wrongRoomId = -5000L;
        var userId = insertMember1Rsp.getUserId();
        var recommendUserId = insertOwnerRsp.getUserId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.exit(wrongRoomId, userId, recommendUserId)
        );

        /* 3. Check same owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 참여자인 경우 | 실패 | 잘못된 사용자번호")
    void exitShareRoomParticipantFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var roomId = insertRoomRsp.getRoomId();
        var wrongUserId = -5000L;
        var recommendUserId = insertOwnerRsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> roomService.exit(roomId, wrongUserId, recommendUserId)
        );

        /* 3. Check same owner */
        var findRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
    }

    @Test
    @DisplayName("방 나가기 -> 공유방 참여자인 경우 | 실패 | 참여자가 아닌 사용자번호")
    void exitShareRoomParticipantFailToNotParticipantUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(List.of(insertMember1Rsp.getUserId()))
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var roomId = insertRoomRsp.getRoomId();
        var NotParticipantUserId = insertMember2Rsp.getUserId();
        var recommendUserId = insertMember1Rsp.getUserId();
        assertThrows(
                RoomNotParticipantException.class,
                () -> roomService.exit(roomId, NotParticipantUserId, recommendUserId)
        );
    }


    // life cycle: @Before -> @Test => separate => Not maintained 
    // Call function in @Test function => maintained 
    void setBaseData() {
        /* 1. Create Owner, Member1, Member2 */
        var insertOwnerReq = UserReqDto.builder()
                .snsType(1)
                .snsId("owner_snsId")
                .pushToken("owner Token")
                .push(true)
                .name("owner")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        insertOwnerRsp = userService.signUp(insertOwnerReq);
        assertThat(insertOwnerRsp).isNotNull();
        assertThat(insertOwnerRsp.getUserId()).isNotNull();

        var insertMember1Req = UserReqDto.builder()
                .snsType(1)
                .snsId("member1_snsId")
                .pushToken("member1 Token")
                .push(true)
                .name("member1")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        insertMember1Rsp = userService.signUp(insertMember1Req);
        assertThat(insertMember1Rsp).isNotNull();
        assertThat(insertMember1Rsp.getUserId()).isNotNull();

        var insertMember2Req = UserReqDto.builder()
                .snsType(1)
                .snsId("member2_snsId")
                .pushToken("member2 Token")
                .push(true)
                .name("member2")
                .birthday("0827")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        insertMember2Rsp = userService.signUp(insertMember2Req);
        assertThat(insertMember2Rsp).isNotNull();
        assertThat(insertMember2Rsp.getUserId()).isNotNull();

        roomMembers = List.of(insertMember1Rsp.getUserId(), insertMember2Rsp.getUserId());
    }

    void assertMembers(RoomRspDto roomRspDto, RoomReqDto roomReqDto) {
        assertThat(roomRspDto.getMembers().size()).isEqualTo(roomReqDto.getMember().size() + 1); // member + owner
        var insertRoomRspMembers = new ArrayList<Long>();
        roomRspDto.getMembers().forEach(member -> {
            if (!insertRoomRspMembers.contains(member.getFriendId())) {
                insertRoomRspMembers.add(member.getFriendId());
            }
        });
        assertThat(insertRoomRspMembers.size()).isEqualTo(roomRspDto.getMembers().size());
    }

}
