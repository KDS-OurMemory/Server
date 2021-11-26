package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotWriterException;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryReqDto;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryRspDto;
import com.kds.ourmemory.controller.v1.memory.dto.ShareType;
import com.kds.ourmemory.controller.v1.room.dto.RoomReqDto;
import com.kds.ourmemory.controller.v1.room.dto.RoomRspDto;
import com.kds.ourmemory.controller.v1.user.dto.UserReqDto;
import com.kds.ourmemory.controller.v1.user.dto.UserRspDto;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.room.RoomService;
import com.kds.ourmemory.service.v1.user.UserService;
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
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    // Base data for test memoryService
    private UserRspDto insertWriterRsp;

    private UserRspDto insertMemberRsp;

    private RoomRspDto insertRoomRsp;

    @Autowired
    private MemoryServiceTest(
            MemoryService memoryService, UserService userService, RoomService roomService
    ) {
        this.memoryService = memoryService;
        this.userService = userService;
        this.roomService = roomService;
    }

    @BeforeAll
    void setUp() {
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
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        var findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);
    }

    @Test
    @Order(2)
    @DisplayName("일정 추가 -> 방 밖(개인 일정 취급)")
    @Transactional
    void addMemoryOutRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());

        /* 2. Find memories */
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        var findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isOne();
    }

    @Test
    @Order(3)
    @DisplayName("일정 추가 -> 개인방")
    @Transactional
    void addMemoryInPrivateRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertWriterRsp.getPrivateRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory to private room */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
    }

    @Test
    @Order(4)
    @DisplayName("일정 참석")
    @Transactional
    void attendMemory() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memory before attend memory and check attendance status */
        var beforeFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(beforeFindMemoryRsp).isNotNull();
        assertTrue(beforeFindMemoryRsp.getUserAttendances().isEmpty());

        /* 3. Attend memory of member */
        var attendRsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(),
                MemoryReqDto.builder()
                        .userId(insertMemberRsp.getUserId())
                        .attendanceStatus(AttendanceStatus.ATTEND)
                        .build()
        );
        assertThat(attendRsp).isNotNull();
        assertThat(attendRsp.getUserAttendances().size()).isOne();

        /* 4. Find memory after attend memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isOne();
    }

    @Test
    @Order(5)
    @DisplayName("일정 불참")
    @Transactional
    void absentMemory() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memory before absence memory and check attendance status */
        var beforeFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(beforeFindMemoryRsp).isNotNull();
        assertTrue(beforeFindMemoryRsp.getUserAttendances().isEmpty());

        /* 3. Absence memory of member */
        var attendRsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(),
                MemoryReqDto.builder()
                        .userId(insertMemberRsp.getUserId())
                        .attendanceStatus(AttendanceStatus.ABSENCE)
                        .build()
        );
        assertThat(attendRsp).isNotNull();
        assertThat(attendRsp.getUserAttendances().size()).isOne();

        /* 4. Find memory after absence memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isOne();
    }

    @Test
    @Order(6)
    @DisplayName("일정 공유 - 개별 사용자 목록")
    @Transactional
    void shareMemoryForUsers() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMember2Req = UserReqDto.builder()
                .snsType(2)
                .snsId("member2_snsId")
                .pushToken("member2 Token")
                .push(true)
                .name("member2")
                .birthday("0527")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        var insertMemberRsp2 = userService.signUp(insertMember2Req);
        assertThat(insertMemberRsp2).isNotNull();
        assertThat(insertMemberRsp2.getUserId()).isNotNull();

        var shareMemoryUsersReq = MemoryReqDto.builder()
                .shareType(ShareType.USERS)
                .shareIds(Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList()))
                .build();

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
        assertThat(shareMemoryRsp.getShareRooms().size()).isEqualTo(3);

        /* 3. find share memory */
        // 1) Check from member
        var findMemberRooms = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findMemberRooms).isNotNull();
        assertThat(findMemberRooms.size()).isEqualTo(2);

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
        assertThat(findMemberRooms2.size()).isOne();

        var findMemberRoomRsp2 = findMemberRooms2.get(0);
        assertThat(findMemberRoomRsp2.getMemories().size()).isOne();
        assertThat(findMemberRoomRsp2.getMembers().size()).isEqualTo(2);
        assertThat(findMemberRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());

        // 3) Check not same member roomId and member2 roomId
        var memberRoomId2 = findMemberRoomRsp2.getRoomId();
        assertNotEquals(memberRoomId.longValue(), memberRoomId2);
    }

    @Test
    @Order(7)
    @DisplayName("일정 공유 - 사용자 그룹")
    @Transactional
    void shareMemoryForUserGroup() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMember2Req = UserReqDto.builder()
                .snsType(2)
                .snsId("member2_snsId")
                .pushToken("member2 Token")
                .push(true)
                .name("member2")
                .birthday("0527")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        var insertMemberRsp2 = userService.signUp(insertMember2Req);
        assertThat(insertMemberRsp2).isNotNull();
        assertThat(insertMemberRsp2.getUserId()).isNotNull();

        var shareMemoryUsersReq = MemoryReqDto.builder()
                .shareType(ShareType.USER_GROUP)
                .shareIds(Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList()))
                .build();

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
        assertThat(shareMemoryRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. find share memory */
        // 1) Check from member1
        var findMemberRooms = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findMemberRooms).isNotNull();
        assertThat(findMemberRooms.size()).isEqualTo(2);

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
        assertThat(findMemberRooms2.size()).isOne();

        var findMemberRoomRsp2 = findMemberRooms2.get(0);
        assertThat(findMemberRoomRsp2.getMemories().size()).isOne();
        assertThat(findMemberRoomRsp2.getMembers().size()).isEqualTo(3);
        assertThat(findMemberRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());

        // 3) check same member1 roomId and member2 roomId
        var memberRoomId2 = findMemberRoomRsp2.getRoomId();
        assertEquals(memberRoomId.longValue(), memberRoomId2);
    }

    @Test
    @Order(8)
    @DisplayName("일정 공유 - 방 목록")
    @Transactional
    void shareMemoryForRooms() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var members2 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2).isNotNull();
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers()).isNotNull();
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq3 = RoomReqDto.builder()
                .name("room name3")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members3)
                .build();
        var insertRoomRsp3 = roomService.insert(insertRoomReq3);
        assertThat(insertRoomRsp3).isNotNull();
        assertThat(insertRoomRsp3.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp3.getMembers()).isNotNull();
        assertThat(insertRoomRsp3.getMembers().size()).isEqualTo(2);

        var shareMemoryUsersReq = MemoryReqDto.builder()
                .shareType(ShareType.ROOMS)
                .shareIds(Stream.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()).collect(toList()))
                .build();

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
        assertThat(shareMemoryRsp.getShareRooms().size()).isEqualTo(3);

        /* 3. Check share memory from original memory */
        var findMemories = memoryService.findMemories(insertWriterRsp.getUserId(), null);
        assertThat(findMemories).isNotNull();
        assertThat(findMemories.size()).isOne();

        var findMemoryRsp = findMemories.get(0);
        assertThat(findMemoryRsp).isNotNull();
        assertThat(findMemoryRsp.getShareRooms().size()).isEqualTo(3);
    }

    @Test
    @Order(9)
    @DisplayName("일정 삭제 - 공유방")
    @Transactional
    void deleteMemoryFromShareRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var deleteMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .targetRoomId(insertRoomRsp.getRoomId())
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        var findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Delete memory from share room */
        var deleteRsp = memoryService.delete(insertMemoryRsp.getMemoryId(), deleteMemoryReq);
        assertNull(deleteRsp);

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
    @Order(10)
    @DisplayName("일정 삭제 - 개인방")
    @Transactional
    void deleteMemoryFromPrivateRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var deleteMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .targetRoomId(insertWriterRsp.getPrivateRoomId())
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        var findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Delete memory from private room */
        var deleteRsp = memoryService.delete(insertMemoryRsp.getMemoryId(), deleteMemoryReq);
        assertNull(deleteRsp);

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

    @Test
    @Order(11)
    @DisplayName("일정 수정 - 작성자")
    @Transactional
    void updateMemoryByWriter() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse("2021-07-08 17:00", alertTimeFormat))
                .endDate(LocalDateTime.parse("2021-07-09 17:00", alertTimeFormat))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        var findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Find before update */
        var beforeFindRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(beforeFindRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());

        /* 4. Update */
        var updateMemoryRsp = memoryService.update(insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), updateReq);
        assertThat(updateMemoryRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(updateMemoryRsp.getContents()).isEqualTo(updateReq.getContents());
        assertThat(updateMemoryRsp.getPlace()).isEqualTo(updateReq.getPlace());
        assertThat(updateMemoryRsp.getStartDate()).isEqualTo(updateReq.getStartDate());
        assertThat(updateMemoryRsp.getEndDate()).isEqualTo(updateReq.getEndDate());
    }

    @Test
    @Order(12)
    @DisplayName("일정 수정 - 작성자 외 다른 사람")
    @Transactional
    void updateMemoryByOther() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-26 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse("2021-07-08 17:00", alertTimeFormat))
                .endDate(LocalDateTime.parse("2021-07-09 17:00", alertTimeFormat))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Find memories */
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        var findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Check Update exception */
        var memoryId = insertMemoryRsp.getMemoryId();
        var memberId = insertMemberRsp.getUserId();
        assertThrows(
                MemoryNotWriterException.class, () -> memoryService.update(memoryId, memberId, updateReq)
        );
    }

    @Test
    @Order(13)
    @DisplayName("일정 목록 조회")
    @Transactional
    void findMemories() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq1 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory1")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-25 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-25 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-24 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq2 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory2")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-24 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-26 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-23 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq3 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory3")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-22 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-23 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-19 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq4 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory4")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-24 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-24 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-22 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq5 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory1")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-22 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-22 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-19 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq6 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory6")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-24 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-25 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-21 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq7 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory7")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse("2022-03-27 17:00", alertTimeFormat)) // 시작시간
                .endDate(LocalDateTime.parse("2022-03-27 18:00", alertTimeFormat)) // 종료시간
                .firstAlarm(LocalDateTime.parse("2022-03-20 17:00", alertTimeFormat)) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memories */
        var insertMemoryRsp1 = memoryService.insert(insertMemoryReq1);
        assertThat(insertMemoryRsp1).isNotNull();

        var insertMemoryRsp2 = memoryService.insert(insertMemoryReq2);
        assertThat(insertMemoryRsp2).isNotNull();

        var insertMemoryRsp3 = memoryService.insert(insertMemoryReq3);
        assertThat(insertMemoryRsp3).isNotNull();

        var insertMemoryRsp4 = memoryService.insert(insertMemoryReq4);
        assertThat(insertMemoryRsp4).isNotNull();

        var insertMemoryRsp5 = memoryService.insert(insertMemoryReq5);
        assertThat(insertMemoryRsp5).isNotNull();

        var insertMemoryRsp6 = memoryService.insert(insertMemoryReq6);
        assertThat(insertMemoryRsp6).isNotNull();

        var insertMemoryRsp7 = memoryService.insert(insertMemoryReq7);
        assertThat(insertMemoryRsp7).isNotNull();

        /* 2. Find memories */
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq1.getUserId(), null);
        assertThat(findMemoriesList.size()).isEqualTo(7);

        // expected order: 3 5 2 4 6 1 7
        var findMemoriesRsp1 = findMemoriesList.get(0);
        assertThat(findMemoriesRsp1.getMemoryId()).isEqualTo(insertMemoryRsp3.getMemoryId());

        var findMemoriesRsp2 = findMemoriesList.get(1);
        assertThat(findMemoriesRsp2.getMemoryId()).isEqualTo(insertMemoryRsp5.getMemoryId());

        var findMemoriesRsp3 = findMemoriesList.get(2);
        assertThat(findMemoriesRsp3.getMemoryId()).isEqualTo(insertMemoryRsp2.getMemoryId());

        var findMemoriesRsp4 = findMemoriesList.get(3);
        assertThat(findMemoriesRsp4.getMemoryId()).isEqualTo(insertMemoryRsp4.getMemoryId());

        var findMemoriesRsp5 = findMemoriesList.get(4);
        assertThat(findMemoriesRsp5.getMemoryId()).isEqualTo(insertMemoryRsp6.getMemoryId());

        var findMemoriesRsp6 = findMemoriesList.get(5);
        assertThat(findMemoriesRsp6.getMemoryId()).isEqualTo(insertMemoryRsp1.getMemoryId());

        var findMemoriesRsp7 = findMemoriesList.get(6);
        assertThat(findMemoriesRsp7.getMemoryId()).isEqualTo(insertMemoryRsp7.getMemoryId());
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create Writer, Member */
        var insertWriterReq = UserReqDto.builder()
                .snsType(2)
                .snsId("writer_snsId")
                .pushToken("member Token")
                .push(true)
                .name("member")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        insertWriterRsp = userService.signUp(insertWriterReq);
        assertThat(insertWriterRsp).isNotNull();
        assertThat(insertWriterRsp.getUserId()).isNotNull();

        var insertMemberReq = UserReqDto.builder()
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
        insertMemberRsp = userService.signUp(insertMemberReq);
        assertThat(insertMemberRsp).isNotNull();
        assertThat(insertMemberRsp.getUserId()).isNotNull();

        /* 2. Create room */
        var members = Stream.of(insertMemberRsp.getUserId()).collect(toList());
        var insertRoomReq = RoomReqDto.builder()
                .name("room name")
                .userId(insertWriterRsp.getUserId())
                .opened(false)
                .member(members)
                .build();
        insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(2);
    }
}
