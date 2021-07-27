package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchTokenDto;
import com.kds.ourmemory.controller.v1.user.dto.UpdateUserDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import com.kds.ourmemory.service.v1.room.RoomService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private final UserService userService;

    private final RoomService roomService;  // The creation process from adding to room.

    private final MemoryService memoryService;  // The creation process from adding to memory.

    /**
     * Assert time format -> delete sec
     *
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    @Autowired
    private UserServiceTest(UserService userService, RoomService roomService, MemoryService memoryService) {
        this.userService = userService;
        this.roomService = roomService;
        this.memoryService = memoryService;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @DisplayName("회원가입-로그인-단일 조회-토큰변경-업데이트")
    @Transactional
    void signUpSignInFind() {
        /* 0. Create Request */
        var insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var patchReq = new PatchTokenDto.Request("patch token");
        var updateReq = new UpdateUserDto.Request("update name", "0927", false, false);

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insertRsp.getJoinDate())).isTrue();

        /* 2. Sign in */
        var signInRsp = userService.signIn(insertReq.getSnsType(), insertReq.getSnsId());
        assertThat(signInRsp).isNotNull();
        assertThat(signInRsp.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(signInRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(signInRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(signInRsp.getBirthday()).isEqualTo(insertReq.getBirthday());

        /* 3. Find */
        var findRsp = userService.find(insertRsp.getUserId());
        assertThat(findRsp).isNotNull();
        assertThat(findRsp.getId()).isEqualTo(insertRsp.getUserId());
        assertThat(findRsp.getSnsType()).isEqualTo(insertReq.getSnsType());
        assertThat(findRsp.getSnsId()).isEqualTo(insertReq.getSnsId());
        assertThat(findRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(findRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertThat(findRsp.isSolar()).isEqualTo(insertReq.isSolar());
        assertThat(findRsp.isBirthdayOpen()).isEqualTo(insertReq.isBirthdayOpen());
        assertThat(findRsp.getPushToken()).isEqualTo(insertReq.getPushToken());

        /* 4. Patch token */
        var patchRsp = userService.patchToken(insertRsp.getUserId(), patchReq);
        assertThat(patchRsp).isNotNull();
        assertThat(isNow(patchRsp.getPatchDate())).isTrue();

        /* 5. Find after patch */
        var afterPatchFindRsp = userService.find(insertRsp.getUserId());
        assertThat(afterPatchFindRsp.getPushToken()).isEqualTo(patchReq.getPushToken());

        /* 6. Update */
        var updateRsp = userService.update(insertRsp.getUserId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertThat(isNow(updateRsp.getUpdateDate())).isTrue();

        /* 7. Find after update */
        var afterUpdateFindRsp = userService.find(insertRsp.getUserId());
        assertThat(afterUpdateFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterUpdateFindRsp.getBirthday()).isEqualTo(updateReq.getBirthday());
        assertThat(afterUpdateFindRsp.isBirthdayOpen()).isEqualTo(updateReq.getBirthdayOpen());
        assertThat(afterUpdateFindRsp.isPush()).isEqualTo(updateReq.getPush());
    }

    @Test
    @Order(2)
    @DisplayName("사용자 목록 조회")
    @Transactional
    void findUsers() {
        /* 0. Create Request */
        var insertUniqueNameReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        var insertSameNameReq1 = new InsertUserDto.Request(
                2, "TESTS_SNS_ID2", "before pushToken",
                "동명이인", "0724", false,
                true, DeviceOs.IOS
        );

        var insertSameNameReq2 = new InsertUserDto.Request(
                3, "TESTS_SNS_ID3", "before pushToken",
                "동명이인", "0720", true,
                false, DeviceOs.ANDROID
        );

        /* 1. Insert */
        var insertUniqueNameRsp = userService.signUp(insertUniqueNameReq);
        assertThat(insertUniqueNameRsp).isNotNull();
        assertThat(insertUniqueNameRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insertUniqueNameRsp.getJoinDate())).isTrue();

        var insertSameNameRsp1 = userService.signUp(insertSameNameReq1);
        assertThat(insertSameNameRsp1).isNotNull();
        assertThat(insertSameNameRsp1.getJoinDate()).isNotNull();
        assertThat(isNow(insertSameNameRsp1.getJoinDate())).isTrue();

        var insertSameNameRsp2 = userService.signUp(insertSameNameReq2);
        assertThat(insertSameNameRsp2).isNotNull();
        assertThat(insertSameNameRsp2.getJoinDate()).isNotNull();
        assertThat(isNow(insertSameNameRsp2.getJoinDate())).isTrue();
        
        /* 2. Find users */
        // 1) find by id : insertUniqueNameReq
        var findUsersByIdList1 = userService.findUsers(insertUniqueNameRsp.getUserId(), null);
        assertThat(findUsersByIdList1).isNotNull();
        assertThat(findUsersByIdList1.isEmpty()).isFalse();
        assertThat(findUsersByIdList1.size()).isOne();

        var findUsersById1 = findUsersByIdList1.get(0);
        assertThat(findUsersById1).isNotNull();
        assertThat(findUsersById1.getUserId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersById1.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersById1.isSolar()).isEqualTo(insertUniqueNameReq.isSolar());
        assertThat(findUsersById1.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.isBirthdayOpen());
        assertThat(findUsersById1.getBirthday()).isEqualTo(
                insertUniqueNameReq.isBirthdayOpen()? insertUniqueNameReq.getBirthday() : null
        );

        // 2) find by id : insertSameNameReq1
        var findUsersByIdList2 = userService.findUsers(insertSameNameRsp1.getUserId(), null);
        assertThat(findUsersByIdList2).isNotNull();
        assertThat(findUsersByIdList2.isEmpty()).isFalse();
        assertThat(findUsersByIdList2.size()).isOne();

        var findUsersById2 = findUsersByIdList2.get(0);
        assertThat(findUsersById2).isNotNull();
        assertThat(findUsersById2.getUserId()).isEqualTo(insertSameNameRsp1.getUserId());
        assertThat(findUsersById2.getName()).isEqualTo(insertSameNameReq1.getName());
        assertThat(findUsersById2.isSolar()).isEqualTo(insertSameNameReq1.isSolar());
        assertThat(findUsersById2.isBirthdayOpen()).isEqualTo(insertSameNameReq1.isBirthdayOpen());
        assertThat(findUsersById2.getBirthday()).isEqualTo(
                insertSameNameReq1.isBirthdayOpen()? insertSameNameReq1.getBirthday() : null
        );

        // 3) find by id : insertSameNameReq2
        var findUsersByIdList3 = userService.findUsers(insertSameNameRsp2.getUserId(), null);
        assertThat(findUsersByIdList3).isNotNull();
        assertThat(findUsersByIdList3.isEmpty()).isFalse();
        assertThat(findUsersByIdList3.size()).isOne();

        var findUsersById3 = findUsersByIdList3.get(0);
        assertThat(findUsersById3).isNotNull();
        assertThat(findUsersById3.getUserId()).isEqualTo(insertSameNameRsp2.getUserId());
        assertThat(findUsersById3.getName()).isEqualTo(insertSameNameReq2.getName());
        assertThat(findUsersById3.isSolar()).isEqualTo(insertSameNameReq2.isSolar());
        assertThat(findUsersById3.isBirthdayOpen()).isEqualTo(insertSameNameReq2.isBirthdayOpen());
        assertThat(findUsersById3.getBirthday()).isEqualTo(
                insertSameNameReq2.isBirthdayOpen()? insertSameNameReq2.getBirthday() : null
        );

        // 4) find by name : insertUniqueNameReq
        var findUsersByUniqueNameList = userService.findUsers(null, insertUniqueNameReq.getName());
        assertThat(findUsersByUniqueNameList).isNotNull();
        assertThat(findUsersByUniqueNameList.isEmpty()).isFalse();
        assertThat(findUsersByUniqueNameList.size()).isOne();

        var findUsersByUniqueName = findUsersByUniqueNameList.get(0);
        assertThat(findUsersByUniqueName).isNotNull();
        assertThat(findUsersByUniqueName.getUserId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByUniqueName.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersByUniqueName.isSolar()).isEqualTo(insertUniqueNameReq.isSolar());
        assertThat(findUsersByUniqueName.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.isBirthdayOpen());
        assertThat(findUsersByUniqueName.getBirthday()).isEqualTo(
                insertUniqueNameReq.isBirthdayOpen()? insertUniqueNameReq.getBirthday() : null
        );

        // 5) find by name : insertSameNameReq1 or 2
        var findUsersBySameNameList = userService.findUsers(null, insertSameNameReq1.getName());
        assertThat(findUsersBySameNameList).isNotNull();
        assertThat(findUsersBySameNameList.isEmpty()).isFalse();
        assertThat(findUsersBySameNameList.size()).isEqualTo(2);
        
        for (var findUsersBySameName : findUsersBySameNameList) {
            var findUsersBySameNameReq = findUsersBySameName.getUserId() == insertSameNameRsp1.getUserId()?
                    insertSameNameReq1 : insertSameNameReq2;
            var findUsersBySameNameId = findUsersBySameName.getUserId() == insertSameNameRsp1.getUserId() ?
                    insertSameNameRsp1.getUserId() : insertSameNameRsp2.getUserId();

            assertThat(findUsersBySameName).isNotNull();
            assertThat(findUsersBySameName.getUserId()).isEqualTo(findUsersBySameNameId);
            assertThat(findUsersBySameName.getName()).isEqualTo(findUsersBySameNameReq.getName());
            assertThat(findUsersBySameName.isSolar()).isEqualTo(findUsersBySameNameReq.isSolar());
            assertThat(findUsersBySameName.isBirthdayOpen()).isEqualTo(findUsersBySameNameReq.isBirthdayOpen());
            assertThat(findUsersBySameName.getBirthday()).isEqualTo(
                    findUsersBySameNameReq.isBirthdayOpen()? findUsersBySameNameReq.getBirthday() : null
            );
        }
    }

    @Test
    @Order(3)
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

        /* 0-2. Create room */
        var insertPrivateRoomReq = new InsertRoomDto.Request(
                "개인방", insertUserRsp.getUserId(), false, new ArrayList<>()
        );
        var insertPrivateRoomRsp = roomService.insert(insertPrivateRoomReq);
        assertThat(insertPrivateRoomRsp).isNotNull();
        assertThat(insertPrivateRoomRsp.getMembers()).isNotNull();
        assertThat(insertPrivateRoomRsp.getOwnerId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertPrivateRoomRsp.getMembers().size()).isOne();

        /* 0-3. Create private memories */
        // 1) not in room memory
        var insertPrivateMemoryReq = new InsertMemoryDto.Request(
                insertUserRsp.getUserId(),
                null,
                "개인 일정(방x)",
                new ArrayList<>(),
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null
        );
        var insertPrivateMemoryRsp = memoryService.insert(insertPrivateMemoryReq);
        assertThat(insertPrivateMemoryRsp).isNotNull();
        assertThat(insertPrivateMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertPrivateMemoryRsp.getMainRoomId()).isNull();
        assertThat(insertPrivateMemoryRsp.getMembers()).isNotNull();
        assertThat(insertPrivateMemoryRsp.getMembers().size()).isOne();

        // 2) private room memory
        var insertPrivateRoomMemoryReq = new InsertMemoryDto.Request(
                insertUserRsp.getUserId(),
                insertPrivateRoomRsp.getRoomId(),
                "개인 일정(방o)",
                new ArrayList<>(),
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null
        );
        var insertPrivateRoomMemoryRsp = memoryService.insert(insertPrivateRoomMemoryReq);
        assertThat(insertPrivateRoomMemoryRsp).isNotNull();
        assertThat(insertPrivateRoomMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertPrivateRoomMemoryRsp.getMainRoomId()).isEqualTo(insertPrivateRoomRsp.getRoomId());
        assertThat(insertPrivateRoomMemoryRsp.getMembers()).isNotNull();
        assertThat(insertPrivateRoomMemoryRsp.getMembers().size()).isOne();

        /* 1. Delete private room owner */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();
        assertThat(isNow(deleteUserRsp.getDeleteDate())).isTrue();

        /* 2. Find room and check delete */
        var roomId = insertPrivateRoomRsp.getRoomId();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(roomId)
        );

        var findRooms = roomService.findRooms(insertUserRsp.getUserId(), null);
        assertThat(findRooms).isNotNull();
        assertThat(findRooms.isEmpty()).isTrue();

        /* 3. Find memories and check delete */
        // 1) private memory(not in room)
        var privateMemoryId = insertPrivateMemoryRsp.getMemoryId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(privateMemoryId)
        );

        var findPrivateMemories = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findPrivateMemories).isNotNull();
        assertThat(findPrivateMemories.isEmpty()).isTrue();

        // 2) private room memory
        var privateRoomMemoryId = insertPrivateRoomMemoryRsp.getMemoryId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(privateRoomMemoryId)
        );

        var findPrivateRoomMemories = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findPrivateRoomMemories).isNotNull();
        assertThat(findPrivateRoomMemories.isEmpty()).isTrue();
    }

    @Test
    @Order(4)
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
        var insertOwnerRoomMemoryReq = new InsertMemoryDto.Request(
                insertUserRsp.getUserId(),
                insertOwnerRoomRsp.getRoomId(),
                "방장 방의 공유 일정",
                Stream.of(insertMemberRsp.getUserId()).collect(Collectors.toList()),
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null
        );
        var insertOwnerRoomMemoryRsp = memoryService.insert(insertOwnerRoomMemoryReq);
        assertThat(insertOwnerRoomMemoryRsp).isNotNull();
        assertThat(insertOwnerRoomMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertOwnerRoomMemoryRsp.getMainRoomId()).isEqualTo(insertOwnerRoomRsp.getRoomId());
        assertThat(insertOwnerRoomMemoryRsp.getMembers()).isNotNull();
        assertThat(insertOwnerRoomMemoryRsp.getMembers().size()).isEqualTo(2);

        /* 1. Delete room owner */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();
        assertThat(isNow(deleteUserRsp.getDeleteDate())).isTrue();

        /* 2. Find room and check transfer owner */
        var findRoomRsp = roomService.find(insertOwnerRoomRsp.getRoomId());
        assertThat(findRoomRsp).isNotNull();
        assertThat(findRoomRsp.getOwnerId()).isNotEqualTo(insertUserRsp.getUserId());
        assertThat(findRoomRsp.getMembers()).isNotNull();
        assertThat(findRoomRsp.getMembers().size()).isEqualTo(1);

        var findRoomsByUser = roomService.findRooms(insertUserRsp.getUserId(), null);
        assertTrue(findRoomsByUser.isEmpty());

        var findRoomsByMember = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findRoomsByMember).isNotNull();
        assertThat(findRoomsByMember.size()).isOne();
        var findRoomsByMemberRsp = findRoomsByMember.get(0);
        assertThat(findRoomsByMemberRsp).isNotNull();
        assertThat(findRoomsByMemberRsp.getRoomId()).isEqualTo(insertOwnerRoomRsp.getRoomId());
        
        /* 3. Find memory */
        var findMemoryRsp = memoryService.find(insertOwnerRoomMemoryRsp.getMemoryId());
        assertThat(findMemoryRsp).isNotNull();
        assertThat(findMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        
        var findMemoriesByUser = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findMemoriesByUser).isNotNull();
        assertThat(findMemoriesByUser.size()).isOne();
        var findMemoriesByUserRsp = findMemoriesByUser.get(0);
        assertThat(findMemoriesByUserRsp).isNotNull();
        assertThat(findMemoriesByUserRsp.getMemoryId()).isEqualTo(insertOwnerRoomMemoryRsp.getMemoryId());

        var findMemoriesByMember = memoryService.findMemories(insertMemberRsp.getUserId(), null);
        assertThat(findMemoriesByMember).isNotNull();
        assertThat(findMemoriesByMember.size()).isOne();
        var findMemoriesByMemberRsp = findMemoriesByMember.get(0);
        assertThat(findMemoriesByMemberRsp).isNotNull();
        assertThat(findMemoriesByMemberRsp.getMemoryId()).isEqualTo(insertOwnerRoomMemoryRsp.getMemoryId());
    }

    @Test
    @Order(5)
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
        var insertParticipantRoomMemoryReq = new InsertMemoryDto.Request(
                insertUserRsp.getUserId(),
                insertParticipantRoomRsp.getRoomId(),
                "참여방 일정",
                Stream.of(insertMemberRsp.getUserId(), insertMember2Rsp.getUserId()).collect(Collectors.toList()),
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF",  // 배경색
                null
        );
        var insertParticipantRoomMemoryRsp = memoryService.insert(insertParticipantRoomMemoryReq);
        assertThat(insertParticipantRoomMemoryRsp).isNotNull();
        assertThat(insertParticipantRoomMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());
        assertThat(insertParticipantRoomMemoryRsp.getMainRoomId()).isEqualTo(insertParticipantRoomRsp.getRoomId());
        assertThat(insertParticipantRoomMemoryRsp.getMembers()).isNotNull();
        assertThat(insertParticipantRoomMemoryRsp.getMembers().size()).isEqualTo(3);

        /* 1. Delete room participant */
        var deleteUserRsp = userService.delete(insertUserRsp.getUserId());
        assertThat(deleteUserRsp).isNotNull();
        assertThat(isNow(deleteUserRsp.getDeleteDate())).isTrue();

        /* 2. Find room and check member */
        var findRoomRsp = roomService.find(insertParticipantRoomRsp.getRoomId());
        assertThat(findRoomRsp).isNotNull();
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(findRoomRsp.getMembers()).isNotNull();
        assertThat(findRoomRsp.getMembers().size()).isEqualTo(2);

        var findRoomsByUser = roomService.findRooms(insertUserRsp.getUserId(), null);
        assertTrue(findRoomsByUser.isEmpty());

        var findRoomsByMember = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findRoomsByMember).isNotNull();
        assertThat(findRoomsByMember.size()).isOne();
        var findRoomsByMemberRsp = findRoomsByMember.get(0);
        assertThat(findRoomsByMemberRsp).isNotNull();
        assertThat(findRoomsByMemberRsp.getRoomId()).isEqualTo(insertParticipantRoomRsp.getRoomId());

        /* 3. Find memory */
        var findMemoryRsp = memoryService.find(insertParticipantRoomMemoryRsp.getMemoryId());
        assertThat(findMemoryRsp).isNotNull();
        assertThat(findMemoryRsp.getWriterId()).isEqualTo(insertUserRsp.getUserId());

        var findMemoriesByUser = memoryService.findMemories(insertUserRsp.getUserId(), null);
        assertThat(findMemoriesByUser).isNotNull();
        assertThat(findMemoriesByUser.size()).isOne();
        var findMemoriesByUserRsp = findMemoriesByUser.get(0);
        assertThat(findMemoriesByUserRsp).isNotNull();
        assertThat(findMemoriesByUserRsp.getMemoryId()).isEqualTo(insertParticipantRoomMemoryRsp.getMemoryId());

        var findMemoriesByMember = memoryService.findMemories(insertMemberRsp.getUserId(), null);
        assertThat(findMemoriesByMember).isNotNull();
        assertThat(findMemoriesByMember.size()).isOne();
        var findMemoriesByMemberRsp = findMemoriesByMember.get(0);
        assertThat(findMemoriesByMemberRsp).isNotNull();
        assertThat(findMemoriesByMemberRsp.getMemoryId()).isEqualTo(insertParticipantRoomMemoryRsp.getMemoryId());
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
