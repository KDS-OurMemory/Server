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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
    @DisplayName("방 안에서 일정 추가")
    @Transactional
    void addMemoryInRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
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
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRsp.getAddedRoomId()).isEqualTo(insertReq.getRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. Find before update */
        FindMemoryDto.Response beforeFindRsp = memoryService.find(insertRsp.getMemoryId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertRsp.getName());
        assertThat(beforeFindRsp.getContents()).isEqualTo(insertRsp.getContents());

        /* 4. Update */
        UpdateMemoryDto.Response updateRsp = memoryService.update(insertRsp.getMemoryId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertTrue(isNow(updateRsp.getUpdateDate()));

        /* 5. Find after update */
        FindMemoryDto.Response afterFindRsp = memoryService.find(insertRsp.getMemoryId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterFindRsp.getContents()).isEqualTo(updateReq.getContents());

        /* 6. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        assertThat(deleteRsp).isNotNull();
        assertTrue(isNow(deleteRsp.getDeleteDate()));

        /* 7. Find after delete */
        Long memoryId = insertRsp.getMemoryId();
        assertThat(memoryId).isNotNull();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId)
        );
    }

    @Test
    @Order(2)
    @DisplayName("방 밖에서 일정 추가")
    @Transactional
    void addMemoryOutRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
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
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());

        /* 2. Find memories */
        List<FindMemoriesDto.Response> findMemoriesList = memoryService.findMemories(insertReq.getUserId(), null);
        assertThat(findMemoriesList).isNotNull();

        findMemoriesList = memoryService.findMemories(null, "Test Memory");
        assertThat(findMemoriesList).isNotNull();

        FindMemoriesDto.Response findMemoriesRsp = findMemoriesList.get(0);
        assertThat(findMemoriesRsp).isNotNull();
        assertThat(findMemoriesRsp.getMemoryId()).isEqualTo(insertRsp.getMemoryId());
        assertThat(findMemoriesRsp.getShareRooms()).isNotNull();
        assertThat(findMemoriesRsp.getShareRooms().size()).isOne();

        /* 3. Find before update */
        FindMemoryDto.Response beforeFindRsp = memoryService.find(insertRsp.getMemoryId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertRsp.getName());
        assertThat(beforeFindRsp.getContents()).isEqualTo(insertRsp.getContents());

        /* 4. Update */
        UpdateMemoryDto.Response updateRsp = memoryService.update(insertRsp.getMemoryId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertTrue(isNow(updateRsp.getUpdateDate()));

        /* 5. Find after update */
        FindMemoryDto.Response afterFindRsp = memoryService.find(insertRsp.getMemoryId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterFindRsp.getContents()).isEqualTo(updateReq.getContents());

        /* 6. Delete memory */
        DeleteMemoryDto.Response deleteRsp = memoryService.delete(insertRsp.getMemoryId());
        assertThat(deleteRsp).isNotNull();
        assertTrue(isNow(deleteRsp.getDeleteDate()));

        /* 7. Find after delete */
        Long memoryId = insertRsp.getMemoryId();
        assertThat(memoryId).isNotNull();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId)
        );
    }

    @Test
    @Order(3)
    @DisplayName("일정 참석")
    @Transactional
    void attendMemory() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
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
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRsp.getAddedRoomId()).isEqualTo(insertReq.getRoomId());

        /* 2. Attend memory of member */
        AttendMemoryDto.Response attendRsp = memoryService.setAttendanceStatus(
                insertRsp.getMemoryId(), insertMemberRsp.getUserId(), AttendanceStatus.ATTEND);
        assertThat(attendRsp).isNotNull();
        assertTrue(isNow(attendRsp.getSetDate()));
    }

    @Test
    @Order(4)
    @DisplayName("일정 불참")
    @Transactional
    void attendanceMemory() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        InsertMemoryDto.Request insertReq = new InsertMemoryDto.Request(
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
        InsertMemoryDto.Response insertRsp = memoryService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRsp.getAddedRoomId()).isEqualTo(insertReq.getRoomId());

        /* 2. Attend memory of member */
        AttendMemoryDto.Response attendRsp = memoryService.setAttendanceStatus(
                insertRsp.getMemoryId(), insertMemberRsp.getUserId(), AttendanceStatus.ABSENCE);
        assertThat(attendRsp).isNotNull();
        assertTrue(isNow(attendRsp.getSetDate()));
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create Writer, Member */
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
                1, "member_snsId", "writer Token",
                "writer", "0720", true,
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
