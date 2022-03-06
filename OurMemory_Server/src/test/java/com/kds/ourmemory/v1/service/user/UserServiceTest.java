package com.kds.ourmemory.v1.service.user;

import com.kds.ourmemory.v1.advice.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.v1.advice.room.exception.RoomNotFoundException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotFoundException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotSignUpException;
import com.kds.ourmemory.v1.controller.friend.dto.FriendReqDto;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryReqDto;
import com.kds.ourmemory.v1.controller.room.dto.RoomReqDto;
import com.kds.ourmemory.v1.controller.room.dto.RoomRspDto;
import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import com.kds.ourmemory.v1.entity.friend.FriendStatus;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import com.kds.ourmemory.v1.service.friend.FriendService;
import com.kds.ourmemory.v1.service.memory.MemoryService;
import com.kds.ourmemory.v1.service.room.RoomService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private final UserService userService;

    private final RoomService roomService;  // The creation process from adding to room.

    private final MemoryService memoryService;  // The creation process from adding to memory.

    private final FriendService friendService;  // Add to pass the status of friends when viewing users

    /**
     * Assert time format -> delete sec
     * <p>
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
    @DisplayName("회원가입 | 성공")
    void signUpSuccess() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("회원가입_SNS_ID")
                .pushToken("회원가입 Token")
                .push(true)
                .name("회원가입 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();
        assertThat(insertRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(insertRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(insertRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertThat(insertRsp.isPush()).isEqualTo(insertReq.getPush());
    }

    @Test
    @DisplayName("회원가입 | 실패 | 요청 Dto 없음.")
    void signUpFailToRequestNull() {
        /* 1. Insert */
        assertThrows(
                NullPointerException.class, () -> userService.signUp(null)
        );
    }

    @Test
    @DisplayName("로그인 | 성공")
    void signInSuccess() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("로그인 성공_SNS_ID")
                .pushToken("로그인 성공 Token")
                .push(true)
                .name("로그인 성공 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var signUpRsp = userService.signUp(insertReq);
        assertThat(signUpRsp).isNotNull();
        assertThat(signUpRsp.getUserId()).isNotNull();

        /* 2. Sign in */
        var signInRsp = userService.signIn(insertReq.getSnsType(), insertReq.getSnsId());
        assertThat(signInRsp).isNotNull();
        assertThat(signInRsp.getUserId()).isEqualTo(signUpRsp.getUserId());
        assertThat(signInRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(signInRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(signInRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertTrue(signInRsp.isPush());
        assertThat(signInRsp.getPrivateRoomId()).isEqualTo(signUpRsp.getPrivateRoomId());
    }

    @Test
    @DisplayName("로그인 | 실패 | snsType 다름")
    void signInFailedToWrongSnsType() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("로그인 실패_SNS_ID")
                .pushToken("로그인 실패 Token")
                .push(true)
                .name("로그인 실패 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var signUpRsp = userService.signUp(insertReq);
        assertThat(signUpRsp).isNotNull();
        assertThat(signUpRsp.getUserId()).isNotNull();

        /* 2. Sign in */
        var wrongSnsType = (insertReq.getSnsType() + 1) % 3;
        var snsId = insertReq.getSnsId();
        assertThrows(
                UserNotSignUpException.class, () -> userService.signIn(wrongSnsType, snsId)
        );
    }

    @Test
    @DisplayName("로그인 | 실패 | snsId 다름")
    void signInFailedToWrongSnsId() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("로그인 실패_SNS_ID")
                .pushToken("로그인 실패 Token")
                .push(true)
                .name("로그인 실패 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var signUpRsp = userService.signUp(insertReq);
        assertThat(signUpRsp).isNotNull();
        assertThat(signUpRsp.getUserId()).isNotNull();

        /* 2. Sign in */
        var snsType = insertReq.getSnsType();
        var wrongSnsId = insertReq.getSnsId() + "wrong!";
        assertThrows(
                UserNotSignUpException.class, () -> userService.signIn(snsType, wrongSnsId)
        );
    }

    @Test
    @DisplayName("내 정보 조회 | 성공")
    void findSuccess() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("내 정보 조회_SNS_ID")
                .pushToken("내 정보 조회 Token")
                .push(true)
                .name("내 정보 조회 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

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
        assertThat(findRsp.isSolar()).isEqualTo(insertReq.getSolar());
        assertThat(findRsp.isBirthdayOpen()).isEqualTo(insertReq.getBirthdayOpen());
        assertThat(findRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
    }

    @Test
    @DisplayName("내 정보 조회 | 실패 | 사용자번호 다름")
    void findFailToWrongUserId() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("내 정보 조회 실패 사용자번호 다름_SNS_ID")
                .pushToken("내 정보 조회 실패 사용자번호 다름 Token")
                .push(true)
                .name("내 정보 조회 실패 사용자번호 다름 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Find */
        var wrongUserId = insertRsp.getUserId() + 1;
        assertThrows(
                UserNotFoundException.class, () -> userService.find(wrongUserId)
        );
    }

    @Test
    @DisplayName("토큰변경 | 성공")
    void patchTokenSuccess() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("토큰변경_SNS_ID")
                .pushToken("토큰변경 Token")
                .push(true)
                .name("토큰변경 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var patchReq = UserReqDto.builder()
                .pushToken("patch token")
                .build();

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
    @DisplayName("토큰변경 | 실패 | 사용자번호 다름")
    void patchTokenFailToWrongUserId() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("토큰변경 실패_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("테스트 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var patchReq = UserReqDto.builder()
                .pushToken("patch token")
                .build();

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();
        assertThat(insertRsp.getPushToken()).isEqualTo(insertReq.getPushToken());

        /* 2. Patch token */
        var wrongUserId = insertRsp.getUserId() + 1;
        assertThrows(
                UserNotFoundException.class, () -> userService.patchToken(wrongUserId, patchReq)
        );
    }

    @Test
    @DisplayName("토큰변경 | 실패 | 토큰값 없음")
    void patchTokenFailToTokenNull() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("테스트 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var wrongPatchReq = UserReqDto.builder()
                .build();

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();
        assertThat(insertRsp.getPushToken()).isEqualTo(insertReq.getPushToken());

        /* 2. Patch token */
        var userId = insertRsp.getUserId();
        assertThrows(
                IllegalArgumentException.class, () -> userService.patchToken(userId, wrongPatchReq)
        );
    }

    @Test
    @DisplayName("업데이트 | 성공")
    void updateSuccess() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("테스트 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var updateReq = UserReqDto.builder()
                .name("update name")
                .birthday("0927")
                .solar(false)
                .birthdayOpen(false)
                .push(false)
                .build();

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
    @DisplayName("업데이트 | 실패 | 사용자번호 다름")
    void updateFailToWrongUserId() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("테스트 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var updateReq = UserReqDto.builder()
                .name("update name")
                .birthday("0927")
                .solar(false)
                .birthdayOpen(false)
                .push(false)
                .build();

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Update */
        var wrongUserId = insertRsp.getUserId() + 1;
        assertThrows(
                UserNotFoundException.class, () -> userService.update(wrongUserId, updateReq)
        );
    }

    @Test
    @DisplayName("업데이트 | 실패 | 요청 Dto 없음")
    void updateFailToRequestNull() {
        /* 0. Create Request */
        var insertReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("테스트 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getUserId()).isNotNull();

        /* 2. Update */
        var userId = insertRsp.getUserId();
        assertThrows(
                NullPointerException.class, () -> userService.update(userId, null)
        );
    }

    @Test
    @DisplayName("사용자 삭제 | 성공")
    void deleteSuccess() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        /* 1. Delete user */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertNull(deleteUserRsp);
    }

    @Test
    @DisplayName("사용자 삭제 | 실패 | 사용자번호 다름")
    void deleteFailToWrongUserId() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        /* 1. Delete user */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertNull(deleteUserRsp);

        /* 2. Check delete user */
        var userId = insertUserRsp.getUserId();
        assertThrows(
                UserNotFoundException.class, () -> userService.find(userId)
        );
    }

    @Test
    @DisplayName("사용자 삭제-친구 처리 | 성공")
    void deleteFriendSuccess() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        // 2) friendUser
        var insertFriendUserReq = UserReqDto.builder()
                .snsType(2)
                .snsId("FRIEND_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("friendUser")
                .birthday("0101")
                .solar(true)
                .birthdayOpen(true)
                .deviceOs(DeviceOs.AOS)
                .build();
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
        assertNull(deleteUserRsp);

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
    @DisplayName("사용자 삭제-개인방/일정 처리 | 성공")
    void deletePrivateUserSuccess() {
        /* 0-1. Create user */
        // 1) user
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
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
        assertNull(deleteUserRsp);

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
    @DisplayName("사용자 삭제-방장 방/일정-방장 처리 | 성공")
    void deleteOwnerUser() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        // 2) member
        var insertMemberReq = UserReqDto.builder()
                .snsType(2)
                .snsId("member1_sns_id")
                .pushToken("member1 Token")
                .push(true)
                .name("member1")
                .birthday("0101")
                .solar(true)
                .birthdayOpen(true)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertMemberRsp = userService.signUp(insertMemberReq);
        assertThat(insertMemberRsp).isNotNull();

        /* 0-2. Create owner room */
        var insertOwnerRoomReq = RoomReqDto.builder()
                .name("방장 방")
                .userId(insertUserRsp.getUserId())
                .opened(false)
                .member(List.of(insertMemberRsp.getUserId()))
                .build();

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
        assertNull(deleteUserRsp);

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
    @DisplayName("사용자 삭제-참여방/일정 처리 | 성공")
    void deleteParticipantUserSuccess() {
        /* 0. Create users */
        // 1) user
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        // 2) member1
        var insertMemberReq = UserReqDto.builder()
                .snsType(2)
                .snsId("member1_sns_id")
                .pushToken("member1 Token")
                .push(true)
                .name("member1")
                .birthday("0101")
                .solar(true)
                .birthdayOpen(true)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertMemberRsp = userService.signUp(insertMemberReq);
        assertThat(insertMemberRsp).isNotNull();

        // 2) member2
        var insertMember2Req = UserReqDto.builder()
                .snsType(3)
                .snsId("member2_sns_id")
                .pushToken("member2 Token")
                .push(true)
                .name("member2")
                .birthday("0501")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        var insertMember2Rsp = userService.signUp(insertMember2Req);
        assertThat(insertMember2Rsp).isNotNull();

        /* 0-2. Create participant room */
        var insertParticipantRoomReq = RoomReqDto.builder()
                .name("참여방")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(List.of(insertUserRsp.getUserId(), insertMember2Rsp.getUserId()))
                .build();

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
        assertNull(deleteUserRsp);

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
    @DisplayName("로그인-사용자 삭제 후 재가입한 사용자 | 성공")
    void reSignUpSignInSuccess() {
        /* 0. Create user */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("로그인-사용자 삭제 후 재가입한 사용자 | 성공_SNS_ID")
                .pushToken("로그인-사용자 삭제 후 재가입한 사용자 | 성공 Token")
                .push(true)
                .name("로그인-사용자 삭제 후 재가입한 사용자 | 성공 이름")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();

        /* 0-1. Sign in before delete */
        var beforeSignInRsp = userService.signIn(insertUserReq.getSnsType(), insertUserReq.getSnsId());
        assertThat(beforeSignInRsp).isNotNull();
        assertEquals(beforeSignInRsp.getUserId(), insertUserRsp.getUserId());

        /* 0-2. Delete user */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertNull(deleteUserRsp);

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
    @DisplayName("프로필 사진 업로드 | 성공")
    void uploadProfileImageSuccess() throws IOException {
        /* 0. Create Request */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserReqDto.builder().profileImage(file).build();

        /* 1. Insert */
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getName()).isEqualTo(insertUserReq.getName());
        assertThat(insertUserRsp.getPushToken()).isEqualTo(insertUserReq.getPushToken());
        assertThat(insertUserRsp.getBirthday()).isEqualTo(insertUserReq.getBirthday());
        assertThat(insertUserRsp.isPush()).isEqualTo(insertUserReq.getPush());

        /* 2. Upload profile image */
        var profileImageRsp = userService.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(profileImageRsp).isNotNull();
        assertThat(profileImageRsp.getProfileImageUrl()).isNotNull();
    }

    @Test
    @DisplayName("프로필 사진 업로드 | 실패 | 사용자번호 다름")
    void uploadProfileImageFailToWrongUserId() throws IOException {
        /* 0. Create Request */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserReqDto.builder().profileImage(file).build();

        /* 1. Insert */
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getName()).isEqualTo(insertUserReq.getName());
        assertThat(insertUserRsp.getPushToken()).isEqualTo(insertUserReq.getPushToken());
        assertThat(insertUserRsp.getBirthday()).isEqualTo(insertUserReq.getBirthday());
        assertThat(insertUserRsp.isPush()).isEqualTo(insertUserReq.getPush());

        /* 2. Upload profile image */
        var wrongUserId = insertUserRsp.getUserId() + 1;
        assertThrows(
                UserNotFoundException.class, () -> userService.uploadProfileImage(wrongUserId, profileImageReq)
        );
    }

    @Test
    @DisplayName("프로필 사진 업로드 | 실패 | 사진없음")
    void uploadProfileImageFailToProfileImageNull() {
        /* 0. Create Request */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var profileImageReq = UserReqDto.builder().build();

        /* 1. Insert */
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getName()).isEqualTo(insertUserReq.getName());
        assertThat(insertUserRsp.getPushToken()).isEqualTo(insertUserReq.getPushToken());
        assertThat(insertUserRsp.getBirthday()).isEqualTo(insertUserReq.getBirthday());
        assertThat(insertUserRsp.isPush()).isEqualTo(insertUserReq.getPush());

        /* 2. Upload profile image */
        var userId = insertUserRsp.getUserId();
        assertThrows(
                NullPointerException.class, () -> userService.uploadProfileImage(userId, profileImageReq)
        );
    }

    @Test
    @DisplayName("프로필 사진 삭제 | 성공")
    void deleteProfileImageSuccess() throws IOException {
        /* 0. Create Request */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserReqDto.builder().profileImage(file).build();

        /* 1. Insert */
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getName()).isEqualTo(insertUserReq.getName());
        assertThat(insertUserRsp.getPushToken()).isEqualTo(insertUserReq.getPushToken());
        assertThat(insertUserRsp.getBirthday()).isEqualTo(insertUserReq.getBirthday());
        assertThat(insertUserRsp.isPush()).isEqualTo(insertUserReq.getPush());

        /* 2. Upload profile image */
        var profileImageRsp = userService.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(profileImageRsp).isNotNull();
        assertThat(profileImageRsp.getProfileImageUrl()).isNotNull();

        /* 3. Delete profile image */
        var deleteProfileImageRsp = userService.deleteProfileImage(insertUserRsp.getUserId());
        assertThat(deleteProfileImageRsp).isNotNull();
        assertNull(deleteProfileImageRsp.getProfileImageUrl());
    }

    @Test
    @DisplayName("프로필 사진 삭제 | 실패 | 사용자번호 다름")
    void deleteProfileImageFailToWrongUserId() throws IOException {
        /* 0. Create Request */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserReqDto.builder().profileImage(file).build();

        /* 1. Insert */
        var insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getName()).isEqualTo(insertUserReq.getName());
        assertThat(insertUserRsp.getPushToken()).isEqualTo(insertUserReq.getPushToken());
        assertThat(insertUserRsp.getBirthday()).isEqualTo(insertUserReq.getBirthday());
        assertThat(insertUserRsp.isPush()).isEqualTo(insertUserReq.getPush());

        /* 2. Upload profile image */
        var profileImageRsp = userService.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(profileImageRsp).isNotNull();
        assertThat(profileImageRsp.getProfileImageUrl()).isNotNull();

        /* 3. Delete profile image */
        var wrongUserId = insertUserRsp.getUserId() + 1;
        assertThrows(
                UserNotFoundException.class, () -> userService.deleteProfileImage(wrongUserId)
        );
    }

    @Test
    @DisplayName("프로필 사진 재업로드 | 성공")
    void reUploadProfileImageSuccess() throws IOException {
        /* 0. Create Request */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before Token")
                .push(true)
                .name("user")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        var file = new MockMultipartFile("favicon",
                "favicon.ico",
                "image/ico",
                new FileInputStream("src/main/resources/static/favicon.ico"));
        var profileImageReq = UserReqDto.builder().profileImage(file).build();

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

        /* 4. Re upload profile image */
        var reProfileImageRsp = userService.uploadProfileImage(insertUserRsp.getUserId(), profileImageReq);
        assertThat(reProfileImageRsp).isNotNull();
        assertThat(reProfileImageRsp.getProfileImageUrl()).isNotNull();
        assertNotSame(reProfileImageRsp.getProfileImageUrl(), profileImageRsp.getProfileImageUrl());
    }

}
