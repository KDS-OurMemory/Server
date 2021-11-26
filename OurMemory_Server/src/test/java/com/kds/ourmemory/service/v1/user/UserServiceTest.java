package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.friend.dto.FriendReqDto;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryReqDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.RoomRspDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchTokenDto;
import com.kds.ourmemory.controller.v1.user.dto.UpdateUserDto;
import com.kds.ourmemory.controller.v1.user.dto.UploadProfileImageDto;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.friend.FriendService;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import com.kds.ourmemory.service.v1.room.RoomService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private final UserService userService;

    private final RoomService roomService;  // The creation process from adding to room.

    private final MemoryService memoryService;  // The creation process from adding to memory.

    private final FriendService friendService;  // Add to pass the status of friends when viewing users

    /**
     * Assert time format -> delete sec
     *
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    @Autowired
    private UserServiceTest(
            UserService userService, RoomService roomService, MemoryService memoryService, FriendService friendService
    ) {
        this.userService = userService;
        this.roomService = roomService;
        this.memoryService = memoryService;
        this.friendService = friendService;
    }

    @BeforeAll
    void setUp() {
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @DisplayName("회원가입")
    @Transactional
    void signUp() {
        /* 0. Create Request */
        var insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("로그인")
    @Transactional
    void signIn() {
        /* 0. Create Request */
        var insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Sign in */
        var signInRsp = userService.signIn(insertReq.getSnsType(), insertReq.getSnsId());
        assertThat(signInRsp).isNotNull();
        assertThat(signInRsp.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(signInRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(signInRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(signInRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertTrue(signInRsp.isPush());
        assertThat(signInRsp.getPrivateRoomId()).isEqualTo(insertRsp.getPrivateRoomId());
    }

    @Test
    @Order(3)
    @DisplayName("단일조회")
    @Transactional
    void find() {
        /* 0. Create Request */
        var insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Find */
        var findRsp = userService.find(insertRsp.getUserId());
        assertThat(findRsp).isNotNull();
        assertThat(findRsp.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(findRsp.getSnsType()).isEqualTo(insertReq.getSnsType());
        assertThat(findRsp.getSnsId()).isEqualTo(insertReq.getSnsId());
        assertThat(findRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(findRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertThat(findRsp.isSolar()).isEqualTo(insertReq.isSolar());
        assertThat(findRsp.isBirthdayOpen()).isEqualTo(insertReq.isBirthdayOpen());
        assertThat(findRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
    }

    @Test
    @Order(4)
    @DisplayName("토큰변경")
    @Transactional
    void patchToken() {
        /* 0. Create Request */
        var insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var patchReq = new PatchTokenDto.Request("patch token");

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();
        assertThat(insertRsp.getPushToken()).isEqualTo(insertReq.getPushToken());

        /* 2. Patch token */
        var patchRsp = userService.patchToken(insertRsp.getUserId(), patchReq);
        assertThat(patchRsp).isNotNull();
        assertThat(patchRsp.getPushToken()).isEqualTo(patchReq.getPushToken());
    }

    @Test
    @Order(5)
    @DisplayName("업데이트")
    @Transactional
    void update() {
        /* 0. Create Request */
        var insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var updateReq = new UpdateUserDto.Request("update name", "0927", false, false, false);

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Update */
        var updateRsp = userService.update(insertRsp.getUserId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertThat(updateRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(updateRsp.getBirthday()).isEqualTo(updateReq.getBirthday());
        assertThat(updateRsp.isSolar()).isEqualTo(updateReq.getSolar());
        assertThat(updateRsp.isBirthdayOpen()).isEqualTo(updateReq.getBirthdayOpen());
        assertThat(updateRsp.isPush()).isEqualTo(updateReq.getPush());
    }

    @Test
    @Order(7)
    @DisplayName("사용자 삭제-친구")
    @Transactional
    void deleteFriend() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        // 2) friendUser
        var insertFriendUserReq = new InsertUserDto.Request(
                2, "member1 sns id", "member1 pushToken",
                "멤버1", "0101", true,
                true, DeviceOs.ANDROID
        );
        var insertFriendUserRsp = userService.signUp(insertFriendUserReq);
        assertThat(insertFriendUserRsp).isNotNull();

        /* 0-2. Create friend */
        var requestFriendRsp = friendService.requestFriend(
                new FriendReqDto(insertUserRsp.getUserId(), insertFriendUserRsp.getUserId())
        );
        assertThat(requestFriendRsp).isNotNull();

        var acceptFriendRsp = friendService.acceptFriend(
                new FriendReqDto(insertFriendUserRsp.getUserId(), insertUserRsp.getUserId())
        );
        assertThat(acceptFriendRsp).isNotNull();

        /* 0-3. Find friend before delete */
        // 1) user side
        var beforeFindFriendsUserSide = friendService.findFriends(insertUserRsp.getUserId());
        assertThat(beforeFindFriendsUserSide).isNotNull();
        assertThat(beforeFindFriendsUserSide.size()).isOne();

        var beforeFindFriendsRspUserSide = beforeFindFriendsUserSide.get(0);
        assertThat(beforeFindFriendsRspUserSide).isNotNull();
        assertThat(beforeFindFriendsRspUserSide.getFriendId()).isEqualTo(insertFriendUserRsp.getUserId());
        assertThat(beforeFindFriendsRspUserSide.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) friendUser side
        var beforeFindFriendsFriendUserSide = friendService.findFriends(insertFriendUserRsp.getUserId());
        assertThat(beforeFindFriendsFriendUserSide).isNotNull();
        assertThat(beforeFindFriendsFriendUserSide.size()).isOne();

        var beforeFindFriendsRspFriendUserSide = beforeFindFriendsFriendUserSide.get(0);
        assertThat(beforeFindFriendsRspFriendUserSide).isNotNull();
        assertThat(beforeFindFriendsRspFriendUserSide.getFriendId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(beforeFindFriendsRspFriendUserSide.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 1. Delete user */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();
        assertFalse(deleteUserRsp.isUsed());

        /* 2. Check delete user */
        var userId = insertUserRsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> userService.find(userId)
        );

        /* 3. Find friend from friendUser and check delete */
        var findFriendsFriendSide = friendService.findFriends(insertFriendUserRsp.getUserId());
        assertThat(findFriendsFriendSide).isNotNull();
        assertTrue(findFriendsFriendSide.isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("사용자 삭제-개인방/일정")
    @Transactional
    void deletePrivateUser() {
        /* 0-1. Create user */
        // 1) user
        var insertUserReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        /* 0-2. Create private memories */
        // 1) not in room memory
        var insertPrivateMemoryReq = MemoryReqDto.builder()
                .userId(insertUserRsp.getUserId())
                .name("개인 일정")
                .contents("방 밖에서 생성")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();
        var insertPrivateMemoryRsp = memoryService.insert(insertPrivateMemoryReq);
        assertThat(insertPrivateMemoryRsp).isNotNull();
        assertThat(insertPrivateMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertPrivateMemoryRsp.getAddedRoomId()).isEqualTo(insertUserRsp.getPrivateRoomId());

        // 2) private room memory
        var insertPrivateRoomMemoryReq = MemoryReqDto.builder()
                .userId(insertUserRsp.getUserId())
                .roomId(insertUserRsp.getPrivateRoomId())
                .name("개인 일정")
                .contents("방 안에서 생성")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();
        var insertPrivateRoomMemoryRsp = memoryService.insert(insertPrivateRoomMemoryReq);
        assertThat(insertPrivateRoomMemoryRsp).isNotNull();
        assertThat(insertPrivateRoomMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertPrivateRoomMemoryRsp.getAddedRoomId()).isEqualTo(insertUserRsp.getPrivateRoomId());

        /* 1. Delete private room owner */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();
        assertFalse(deleteUserRsp.isUsed());

        /* 2. Check delete user */
        var userId = insertUserRsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> userService.find(userId)
        );

        /* 3. Find room and check delete */
        var roomId = insertUserRsp.getPrivateRoomId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(roomId)
        );

        var findRooms = roomService.findRooms(insertUserRsp.getUserId(), null);
        assertThat(findRooms).isNotNull();
        assertThat(findRooms.isEmpty()).isTrue();

        /* 4. Find memories and check delete */
        // 1) private memory(not in room)
        var privateMemoryId = insertPrivateMemoryRsp.getMemoryId();
        var privateMemoryRoomId = insertPrivateMemoryRsp.getAddedRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(privateMemoryId, privateMemoryRoomId)
        );

        var findPrivateMemories = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findPrivateMemories).isNotNull();
        assertThat(findPrivateMemories.isEmpty()).isTrue();

        // 2) private room memory
        var privateRoomMemoryId = insertPrivateRoomMemoryRsp.getMemoryId();
        var privateRoomRoomId = insertPrivateRoomMemoryRsp.getAddedRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(privateRoomMemoryId, privateRoomRoomId)
        );

        var findPrivateRoomMemories = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findPrivateRoomMemories).isNotNull();
        assertThat(findPrivateRoomMemories.isEmpty()).isTrue();
    }

    @Test
    @Order(9)
    @DisplayName("사용자 삭제-방장 방/일정")
    @Transactional
    void deleteOwnerUser() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        // 2) member
        var insertMemberReq = new InsertUserDto.Request(
                2, "member1 sns id", "member1 pushToken",
                "멤버1", "0101", true,
                true, DeviceOs.ANDROID
        );
        var insertMemberRsp = userService.signUp(insertMemberReq);
        assertThat(insertMemberRsp).isNotNull();

        /* 0-2. Create owner room */
        var insertOwnerRoomReq = new InsertRoomDto.Request(
                "방장 방", insertUserRsp.getUserId(), false,
                Stream.of(insertMemberRsp.getUserId()).collect(Collectors.toList())
        );
        var insertOwnerRoomRsp = roomService.insert(insertOwnerRoomReq);
        assertThat(insertOwnerRoomRsp).isNotNull();
        assertThat(insertOwnerRoomRsp.getOwnerId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertOwnerRoomRsp.getMembers()).isNotNull();
        assertThat(insertOwnerRoomRsp.getMembers().size()).isEqualTo(2);

        /* 0-3. Create owner room memory */
        var insertOwnerRoomMemoryReq = MemoryReqDto.builder()
                .userId(insertUserRsp.getUserId())
                .roomId(insertOwnerRoomRsp.getRoomId())
                .name("방장 방의 공유 일정")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();
        var insertOwnerRoomMemoryRsp = memoryService.insert(insertOwnerRoomMemoryReq);
        assertThat(insertOwnerRoomMemoryRsp).isNotNull();
        assertThat(insertOwnerRoomMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertOwnerRoomMemoryRsp.getAddedRoomId()).isEqualTo(insertOwnerRoomRsp.getRoomId());

        /* 1. Delete room owner */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();
        assertFalse(deleteUserRsp.isUsed());

        /* 2. Check delete user */
        var userId = insertUserRsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> userService.find(userId)
        );

        /* 3. Find room and check transfer owner */
        var findRoomRsp = roomService.find(insertOwnerRoomRsp.getRoomId());
        assertThat(findRoomRsp).isNotNull();
        assertThat(findRoomRsp.getOwnerId()).isNotEqualTo(insertUserRsp.getUserId());
        assertThat(findRoomRsp.getMembers()).isNotNull();
        assertThat(findRoomRsp.getMembers().size()).isEqualTo(1);

        var findRoomsByUser = roomService.findRooms(insertUserRsp.getUserId(), null);
        assertTrue(findRoomsByUser.isEmpty());

        var findRoomsByMember = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findRoomsByMember).isNotNull();
        assertThat(findRoomsByMember.size()).isEqualTo(1);

        var ownerRoomCnt = 0;
        for (RoomRspDto response : findRoomsByMember) {
            if (response.getRoomId() == insertOwnerRoomRsp.getRoomId()) ownerRoomCnt++;
        }
        assertThat(ownerRoomCnt).isOne();
        
        /* 4. Find memory */
        var ownerRoomMemoryId = insertOwnerRoomMemoryRsp.getMemoryId();
        var ownerRoomRoomId = insertOwnerRoomMemoryRsp.getAddedRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(ownerRoomMemoryId, ownerRoomRoomId)
        );

        var findMemoriesByUser = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findMemoriesByUser).isNotNull();
        assertTrue(findMemoriesByUser.isEmpty());

        var findMemoriesByMember = memoryService.findMemories(insertMemberRsp.getUserId(), null);
        assertThat(findMemoriesByMember).isNotNull();
        assertTrue(findMemoriesByMember.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("사용자 삭제-참여방/일정")
    @Transactional
    void deleteParticipantUser() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        // 2) member1
        var insertMemberReq = new InsertUserDto.Request(
                2, "member1 sns id", "member1 pushToken",
                "멤버1", "0101", true,
                true, DeviceOs.ANDROID
        );
        var insertMemberRsp = userService.signUp(insertMemberReq);
        assertThat(insertMemberRsp).isNotNull();

        // 2) member2
        var insertMember2Req = new InsertUserDto.Request(
                3, "member2 sns id", "member2 pushToken",
                "멤버2", "0201", false,
                true, DeviceOs.IOS
        );
        var insertMember2Rsp = userService.signUp(insertMember2Req);
        assertThat(insertMember2Rsp).isNotNull();

        /* 0-2. Create participant room */
        var insertParticipantRoomReq = new InsertRoomDto.Request(
                "참여방", insertMemberRsp.getUserId(), false,
                Stream.of(insertUserRsp.getUserId(), insertMember2Rsp.getUserId()).collect(Collectors.toList())
        );
        var insertParticipantRoomRsp = roomService.insert(insertParticipantRoomReq);
        assertThat(insertParticipantRoomRsp).isNotNull();
        assertThat(insertParticipantRoomRsp.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertParticipantRoomRsp.getMembers()).isNotNull();
        assertThat(insertParticipantRoomRsp.getMembers().size()).isEqualTo(3);

        /* 0-3. Create participant room memory */
        var insertParticipantRoomMemoryReq = MemoryReqDto.builder()
                .userId(insertUserRsp.getUserId())
                .roomId(insertParticipantRoomRsp.getRoomId())
                .name("참여방 일정")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();
        var insertParticipantRoomMemoryRsp = memoryService.insert(insertParticipantRoomMemoryReq);
        assertThat(insertParticipantRoomMemoryRsp).isNotNull();
        assertThat(insertParticipantRoomMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertParticipantRoomMemoryRsp.getAddedRoomId()).isEqualTo(insertParticipantRoomRsp.getRoomId());

        /* 1. Delete room participant */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();
        assertFalse(deleteUserRsp.isUsed());

        /* 2. Check delete user */
        var userId = insertUserRsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> userService.find(userId)
        );

        /* 3. Find room and check member */
        var findRoomRsp = roomService.find(insertParticipantRoomRsp.getRoomId());
        assertThat(findRoomRsp).isNotNull();
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(findRoomRsp.getMembers()).isNotNull();
        assertThat(findRoomRsp.getMembers().size()).isEqualTo(2);

        var findRoomsByUser = roomService.findRooms(insertUserRsp.getUserId(), null);
        assertTrue(findRoomsByUser.isEmpty());

        var findRoomsByMember = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findRoomsByMember).isNotNull();
        assertThat(findRoomsByMember.size()).isEqualTo(1);
        var ownerRoomCnt = 0;
        for (RoomRspDto response : findRoomsByMember) {
            if (response.getRoomId() == insertParticipantRoomRsp.getRoomId()) ownerRoomCnt++;
        }
        assertThat(ownerRoomCnt).isOne();

        /* 4. Find memory */
        var participantRoomMemoryId = insertParticipantRoomMemoryRsp.getMemoryId();
        var participantRoomRoomId = insertParticipantRoomMemoryRsp.getAddedRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(participantRoomMemoryId, participantRoomRoomId)
        );

        var findMemoriesByUser = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findMemoriesByUser).isNotNull();
        assertTrue(findMemoriesByUser.isEmpty());

        var findMemoriesByMember = memoryService.findMemories(insertMemberRsp.getUserId(), null);
        assertThat(findMemoriesByMember).isNotNull();
        assertTrue(findMemoriesByMember.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("로그인-사용자 삭제 후 재가입한 사용자")
    @Transactional
    void reSignUpSignIn() {
        /* 0. Create user */
        var insertUserReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        /* 0-1. Sign in before delete */
        var beforeSignInRsp = userService.signIn(insertUserReq.getSnsType(), insertUserReq.getSnsId());
        assertThat(beforeSignInRsp).isNotNull();
        assertEquals(beforeSignInRsp.getUserId(), insertUserRsp.getUserId());

        /* 0-2. Delete user */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();

        /* 0-3. Check delete user */
        var userId = insertUserRsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> userService.find(userId)
        );

        /* 0-4. Re-sign up user */
        var reInsertUserRsp = userService.signUp(insertUserReq);
        assertThat(reInsertUserRsp).isNotNull();

        /* 1. Sign in after Re-sign up */
        var afterSignInRsp = userService.signIn(insertUserReq.getSnsType(), insertUserReq.getSnsId());
        assertThat(afterSignInRsp).isNotNull();
        assertThat(afterSignInRsp.getUserId()).isEqualTo(reInsertUserRsp.getUserId());
        assertNotEquals(afterSignInRsp.getPrivateRoomId(), beforeSignInRsp.getPrivateRoomId());
    }

    @Test
    @Order(12)
    @DisplayName("프로필 사진 업로드/삭제")
    @Transactional
    void uploadDeleteProfileImage() throws IOException {
        /* 0. Create Request */
        var insertUserReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var file = new MockMultipartFile("image",
                "CD 명함사이즈.jpg",
                "image/jpg",
                new FileInputStream("F:\\자료\\문서\\서류 및 신분증 사진\\CD 명함사이즈.jpg"));
        var profileImageReq = new UploadProfileImageDto.Request(file);

        /* 1. Insert */
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();

        /* 2. Find User */
        var findUserRsp = userService.find(insertUserRsp.getUserId());
        assertThat(findUserRsp).isNotNull();
        assertThat(findUserRsp.getProfileImageUrl()).isNull();

        /* 3. Upload profile image */
        var profileImageRsp = userService.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(profileImageRsp).isNotNull();
        assertThat(profileImageRsp.getProfileImageUrl()).isNotNull();

        /* 4. Find User after upload profile image */
        var afterFindUserRsp = userService.find(insertUserRsp.getUserId());
        assertThat(afterFindUserRsp).isNotNull();
        assertThat(afterFindUserRsp.getProfileImageUrl()).isEqualTo(profileImageRsp.getProfileImageUrl());

        /* 5. Re upload profile image */
        var reProfileImageRsp = userService.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(reProfileImageRsp).isNotNull();
        assertThat(reProfileImageRsp.getProfileImageUrl()).isNotNull();
        assertNotSame(reProfileImageRsp.getProfileImageUrl(), profileImageRsp.getProfileImageUrl());

        /* 6. Delete profile image */
        var deleteProfileImageRsp = userService.deleteProfileImage(insertUserRsp.getUserId());
        assertThat(deleteProfileImageRsp).isNotNull();
        assertNull(deleteProfileImageRsp.getProfileImageUrl());
    }
}
