package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.controller.v1.memory.dto.*;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.room.RoomService;
import com.kds.ourmemory.service.v1.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {
    private final MemoryService memoryService;

    private final RoomService roomService;  // The creation process from adding to the deletion of the room.

    private final UserService userService;  // The creation process from adding to the deletion of the memory.

    /**
     * Assert time format -> delete sec
     * 
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    // Base data for test memoryService
    private InsertUserDto.Response insertWriterRsp;

    private InsertUserDto.Response insertMemberRsp;

    private InsertRoomDto.Response insertRoomRsp;

    @Autowired
    private MemoryServiceTest(MemoryService memoryService, UserService userService, RoomService roomService) {
        this.memoryService = memoryService;
        this.userService = userService;
        this.roomService = roomService;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @DisplayName("일정 추가 -> 방 안(공유 일정 취급)")
    @Transactional
    void addMemoryInRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        UpdateMemoryDto.Request updateReq = new UpdateMemoryDto.Request(
                "Update memory name",
                "Update contents",
                "Update place",
                LocalDateTime.parse("2021-07-08 17:00", alertTimeFormat),
                LocalDateTime.parse("2021-07-09 17:00", alertTimeFormat),
                null,
                null,
                null
        );

        /* 1. Make memory */
        InsertMemoryDto.Response insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Find before update */
        FindMemoryDto.Response beforeFindRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(beforeFindRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());

        /* 4. Update */
        UpdateMemoryDto.Response updateRsp = memoryService.update(insertMemoryRsp.getMemoryId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertTrue(isNow(updateRsp.getUpdateDate()));

        /* 5. Find after update */
        FindMemoryDto.Response afterFindRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterFindRsp.getContents()).isEqualTo(updateReq.getContents());
    }

    @Test
    @Order(2)
    @DisplayName("일정 추가 -> 방 밖(개인 일정 취급)")
    @Transactional
    void addMemoryOutRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                null,
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        UpdateMemoryDto.Request updateReq = new UpdateMemoryDto.Request(
                "Update memory name",
                "Update contents",
                "Update place",
                LocalDateTime.parse("2021-07-08 17:00", alertTimeFormat),
                LocalDateTime.parse("2021-07-09 17:00", alertTimeFormat),
                null,
                null,
                null
        );

        /* 1. Make memory */
        InsertMemoryDto.Response insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isOne();

        /* 3. Find before update */
        FindMemoryDto.Response beforeFindRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertWriterRsp.getPrivateRoomId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(beforeFindRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());

        /* 4. Update */
        UpdateMemoryDto.Response updateRsp = memoryService.update(insertMemoryRsp.getMemoryId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertTrue(isNow(updateRsp.getUpdateDate()));

        /* 5. Find after update */
        FindMemoryDto.Response afterFindRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertWriterRsp.getPrivateRoomId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterFindRsp.getContents()).isEqualTo(updateReq.getContents());
    }

    @Test
    @Order(3)
    @DisplayName("일정 참석")
    @Transactional
    void attendMemory() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        /* 1. Make memory */
        InsertMemoryDto.Response insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memory before attend memory and check attendance status */
        var beforeFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(beforeFindMemoryRsp).isNotNull();
        assertTrue(beforeFindMemoryRsp.getUserAttendances().isEmpty());

        /* 3. Attend memory of member */
        AttendMemoryDto.Response attendRsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(), insertMemberRsp.getUserId(), AttendanceStatus.ATTEND);
        assertThat(attendRsp).isNotNull();
        assertTrue(isNow(attendRsp.getSetDate()));

        /* 4. Find memory after attend memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isOne();
    }

    @Test
    @Order(4)
    @DisplayName("일정 불참")
    @Transactional
    void attendanceMemory() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        /* 1. Make memory */
        InsertMemoryDto.Response insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memory before absence memory and check attendance status */
        var beforeFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(beforeFindMemoryRsp).isNotNull();
        assertTrue(beforeFindMemoryRsp.getUserAttendances().isEmpty());

        /* 3. Absence memory of member */
        AttendMemoryDto.Response attendRsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(), insertMemberRsp.getUserId(), AttendanceStatus.ABSENCE);
        assertThat(attendRsp).isNotNull();
        assertTrue(isNow(attendRsp.getSetDate()));

        /* 4. Find memory after absence memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isOne();
    }

    @Test
    @Order(5)
    @DisplayName("일정 공유 - 개별 사용자 목록")
    @Transactional
    void shareMemoryForUsers() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        var insertMember2Req = new InsertUserDto.Request(
                2, "member2_snsId", "member2 Token",
                "member2", "0527", true,
                false, DeviceOs.IOS
        );
        InsertUserDto.Response insertMemberRsp2 = userService.signUp(insertMember2Req);
        assertThat(insertMemberRsp2).isNotNull();
        assertThat(insertMemberRsp2.getUserId()).isNotNull();
        assertThat(insertMemberRsp2.getPrivateRoomId()).isNotNull();
        assertTrue(isNow(insertMemberRsp2.getJoinDate()));

        var shareMemoryUsersReq = new ShareMemoryDto.Request(
                ShareMemoryDto.ShareType.USERS,
                Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList())
        );

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for users */
        var shareMemoryRsp = memoryService.shareMemory(
                insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), shareMemoryUsersReq
        );
        assertThat(shareMemoryRsp).isNotNull();
        assertTrue(isNow(shareMemoryRsp.getShareDate()));

        /* 3. find share memory */
        // 1) Check from member
        var findMemberRooms = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findMemberRooms).isNotNull();
        assertThat(findMemberRooms.size()).isEqualTo(3);

        var isValidMember = false;
        Long memberRoomId = null;
        for (var findMemberRoomRsp : findMemberRooms) {
            if (findMemberRoomRsp.getRoomId() == insertRoomRsp.getRoomId() 
                    || findMemberRoomRsp.getRoomId() == insertMemberRsp.getPrivateRoomId()
            ) {
                continue;
            }

            assertThat(findMemberRoomRsp.getMemories().size()).isOne();
            assertThat(findMemberRoomRsp.getMembers().size()).isEqualTo(2);
            assertThat(findMemberRoomRsp.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
            isValidMember = true;
            memberRoomId = findMemberRoomRsp.getRoomId();
        }
        assertTrue(isValidMember);

        // 2) Check from member2
        var findMemberRooms2 = roomService.findRooms(insertMemberRsp2.getUserId(), null);
        assertThat(findMemberRooms2).isNotNull();
        assertThat(findMemberRooms2.size()).isEqualTo(2);

        var isValidMember2 = false;
        Long memberRoomId2 = null;
        for (var findMemberRoomRsp2 : findMemberRooms2) {
            if (findMemberRoomRsp2.getRoomId() == insertMemberRsp2.getPrivateRoomId())
                continue;

            assertThat(findMemberRoomRsp2.getMemories().size()).isOne();
            assertThat(findMemberRoomRsp2.getMembers().size()).isEqualTo(2);
            assertThat(findMemberRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
            isValidMember2 = true;
            memberRoomId2 = findMemberRoomRsp2.getRoomId();
        }
        assertTrue(isValidMember2);

        // 3) Check not same member roomId and member2 roomId
        assertNotEquals(memberRoomId.longValue(), memberRoomId2.longValue());
    }

    @Test
    @Order(6)
    @DisplayName("일정 공유 - 사용자 그룹")
    @Transactional
    void shareMemoryForUserGroup() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        var insertMember2Req = new InsertUserDto.Request(
                2, "member2_snsId", "member2 Token",
                "member2", "0527", true,
                false, DeviceOs.IOS
        );
        InsertUserDto.Response insertMemberRsp2 = userService.signUp(insertMember2Req);
        assertThat(insertMemberRsp2).isNotNull();
        assertThat(insertMemberRsp2.getUserId()).isNotNull();
        assertThat(insertMemberRsp2.getPrivateRoomId()).isNotNull();
        assertTrue(isNow(insertMemberRsp2.getJoinDate()));

        var shareMemoryUsersReq = new ShareMemoryDto.Request(
                ShareMemoryDto.ShareType.USER_GROUP,
                Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList())
        );

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for user group */
        var shareMemoryRsp = memoryService.shareMemory(
                insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), shareMemoryUsersReq
        );
        assertThat(shareMemoryRsp).isNotNull();
        assertTrue(isNow(shareMemoryRsp.getShareDate()));

        /* 3. find share memory */
        // 1) Check from member1
        var findMemberRooms = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findMemberRooms).isNotNull();
        assertThat(findMemberRooms.size()).isEqualTo(3);

        var isValidMember = false;
        Long memberRoomId = null;
        for (var findMemberRoomRsp : findMemberRooms) {
            if (findMemberRoomRsp.getRoomId() == insertRoomRsp.getRoomId()
                    || findMemberRoomRsp.getRoomId() == insertMemberRsp.getPrivateRoomId()
            ) {
                continue;
            }

            assertThat(findMemberRoomRsp.getMemories().size()).isOne();
            assertThat(findMemberRoomRsp.getMembers().size()).isEqualTo(3);
            assertThat(findMemberRoomRsp.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
            isValidMember = true;
            memberRoomId = findMemberRoomRsp.getRoomId();
        }
        assertTrue(isValidMember);

        // 2) Check from member2
        var findMemberRooms2 = roomService.findRooms(insertMemberRsp2.getUserId(), null);
        assertThat(findMemberRooms2).isNotNull();
        assertThat(findMemberRooms2.size()).isEqualTo(2);

        var isValidMember2 = false;
        Long memberRoomId2 = null;
        for (var findMemberRoomRsp2 : findMemberRooms2) {
            if (findMemberRoomRsp2.getRoomId() == insertMemberRsp2.getPrivateRoomId())
                continue;

            assertThat(findMemberRoomRsp2.getMemories().size()).isOne();
            assertThat(findMemberRoomRsp2.getMembers().size()).isEqualTo(3);
            assertThat(findMemberRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
            isValidMember2 = true;
            memberRoomId2 = findMemberRoomRsp2.getRoomId();
        }
        assertTrue(isValidMember2);

        // 3) check same member1 roomId and member2 roomId
        assertEquals(memberRoomId.longValue(), memberRoomId2.longValue());
    }

    @Test
    @Order(7)
    @DisplayName("일정 공유 - 방 목록")
    @Transactional
    void shareMemoryForRooms() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        var members2 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq2 = new InsertRoomDto.Request("room name2", insertMemberRsp.getUserId(), false, members2);
        InsertRoomDto.Response insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2).isNotNull();
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers()).isNotNull();
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq3 = new InsertRoomDto.Request("room name2", insertMemberRsp.getUserId(), false, members3);
        InsertRoomDto.Response insertRoomRsp3 = roomService.insert(insertRoomReq3);
        assertThat(insertRoomRsp3).isNotNull();
        assertThat(insertRoomRsp3.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp3.getMembers()).isNotNull();
        assertThat(insertRoomRsp3.getMembers().size()).isEqualTo(2);

        var shareMemoryUsersReq = new ShareMemoryDto.Request(
                ShareMemoryDto.ShareType.ROOMS,
                Stream.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()).collect(toList())
        );

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for user group */
        var shareMemoryRsp = memoryService.shareMemory(
                insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), shareMemoryUsersReq
        );
        assertThat(shareMemoryRsp).isNotNull();
        assertTrue(isNow(shareMemoryRsp.getShareDate()));

        /* 3. Check share memory from original memory */
        var findMemories = memoryService.findMemories(insertWriterRsp.getUserId(), null);
        assertThat(findMemories).isNotNull();
        assertThat(findMemories.size()).isOne();

        var findMemoryRsp = findMemories.get(0);
        assertThat(findMemoryRsp).isNotNull();
        assertThat(findMemoryRsp.getShareRooms().size()).isEqualTo(4);
    }

    @Test
    @Order(8)
    @DisplayName("일정 삭제 - 공유방")
    @Transactional
    void deleteMemoryFromShareRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        DeleteMemoryDto.Request deleteMemoryReq = new DeleteMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId()
        );

        /* 1. Make memory */
        InsertMemoryDto.Response insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Delete memory from share room */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertMemoryRsp.getMemoryId(), deleteMemoryReq);
        assertThat(deleteRsp).isNotNull();
        assertTrue(isNow(deleteRsp.getDeleteDate()));

        /* 4. Find memory after delete */
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp).isNotNull();
        assertThat(findMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());

        /* 5. Find memory after delete from private room */
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();

        /* 6. Find memory after delete from share room */
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().size()).isZero();
    }

    @Test
    @Order(9)
    @DisplayName("일정 삭제 - 개인방")
    @Transactional
    void deleteMemoryFromPrivateRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertMemoryReq = new InsertMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertRoomRsp.getRoomId(),
                "Test Memory",
                "Test Contents",
                "Test Place",
                LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat), // 시작 시간
                LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat), // 종료 시간
                LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat), // 첫 번째 알림
                null,       // 두 번째 알림
                "#FFFFFF"  // 배경색
        );

        DeleteMemoryDto.Request deleteMemoryReq = new DeleteMemoryDto.Request(
                insertWriterRsp.getUserId(),
                insertWriterRsp.getPrivateRoomId()
        );

        /* 1. Make memory */
        InsertMemoryDto.Response insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Delete memory from private room */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertMemoryRsp.getMemoryId(), deleteMemoryReq);
        assertThat(deleteRsp).isNotNull();
        assertTrue(isNow(deleteRsp.getDeleteDate()));

        /* 4. Find memory after delete */
        var memoryId = insertMemoryRsp.getMemoryId();
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId, roomId)
        );

        /* 5. Find memory after delete from private room */
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isZero();

        /* 6. Find memory after delete from share room */
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().size()).isZero();
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create Writer, Member1, Member2 */
        var insertWriterReq = new InsertUserDto.Request(
                1, "writer_snsId", "member Token",
                "member", "0519", true,
                false, DeviceOs.IOS
        );
        insertWriterRsp = userService.signUp(insertWriterReq);
        assertThat(insertWriterRsp).isNotNull();
        assertThat(insertWriterRsp.getUserId()).isNotNull();
        assertThat(insertWriterRsp.getPrivateRoomId()).isNotNull();
        assertTrue(isNow(insertWriterRsp.getJoinDate()));

        var insertMemberReq = new InsertUserDto.Request(
                1, "member1_snsId", "member1 Token",
                "member1", "0720", true,
                false, DeviceOs.ANDROID
        );
        insertMemberRsp = userService.signUp(insertMemberReq);
        assertThat(insertMemberRsp).isNotNull();
        assertThat(insertMemberRsp.getUserId()).isNotNull();
        assertThat(insertMemberRsp.getPrivateRoomId()).isNotNull();
        assertTrue(isNow(insertMemberRsp.getJoinDate()));

        /* 2. Create room */
        var members = Stream.of(insertMemberRsp.getUserId()).collect(toList());
        var insertRoomReq = new InsertRoomDto.Request("room name", insertWriterRsp.getUserId(), false, members);
        insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(2);
    }

    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
