package com.kds.ourmemory.v1.service.memory;

import com.kds.ourmemory.v1.advice.memory.exception.*;
import com.kds.ourmemory.v1.advice.room.exception.RoomNotFoundException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotFoundException;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryReqDto;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryRspDto;
import com.kds.ourmemory.v1.controller.memory.dto.ShareType;
import com.kds.ourmemory.v1.controller.room.dto.RoomReqDto;
import com.kds.ourmemory.v1.controller.room.dto.RoomRspDto;
import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import com.kds.ourmemory.v1.controller.user.dto.UserRspDto;
import com.kds.ourmemory.v1.entity.relation.AttendanceStatus;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import com.kds.ourmemory.v1.service.room.RoomService;
import com.kds.ourmemory.v1.service.user.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemoryServiceTest {
    private final MemoryService memoryService;

    private final RoomService roomService;  // The creation process from adding to the deletion of the room.

    private final UserService userService;  // The creation process from adding to the deletion of the memory.

    /**
     * Assert time format -> delete sec
     * <p>
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format
    private DateTimeFormatter dateFormat;

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
        dateFormat = DateTimeFormatter.ofPattern("yyyy-MM");
    }

    @Test
    @DisplayName("일정 추가 -> 방 안(공유 일정 취급) | 성공")
    void insertMemoryInRoomSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());
    }

    @Test
    @DisplayName("일정 추가 -> 방 안(공유 일정 취급) | 실패 | 사용자번호 다름")
    void insertMemoryInRoomFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId() + 50000)
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        assertThrows(
                MemoryNotFoundWriterException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 추가 -> 방 안(공유 일정 취급) | 실패 | 탈퇴한 일정 작성자번호")
    void insertMemoryInRoomFailToDeactivateUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Set deactivate user */
        var deactivateWriterReq = UserReqDto.builder()
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
        var deactivateWriterRsp = userService.signUp(deactivateWriterReq);
        assertThat(deactivateWriterRsp).isNotNull();
        assertThat(deactivateWriterRsp.getUserId()).isNotNull();

        userService.delete(deactivateWriterRsp.getUserId());

        /* 0-3. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(deactivateWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        assertThrows(
                MemoryDeactivateWriterException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 추가 -> 방 안(공유 일정 취급) | 실패 | 잘못된 방 번호")
    void insertMemoryInRoomFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId() + 50000)
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        assertThrows(
                MemoryNotFoundRoomException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 추가 -> 방 밖(개인 일정 취급) | 성공")
    void insertMemoryOutRoomSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());
    }

    @Test
    @DisplayName("일정 추가 -> 방 밖(개인 일정 취급) | 실패 | 잘못된 사용자번호")
    void insertMemoryOutRoomFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId() + 50000)
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        assertThrows(
                MemoryNotFoundWriterException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 추가 -> 방 밖(개인 일정 취급) | 실패 | 탈퇴한 일정 작성자번호")
    void insertMemoryOutRoomFailToDeactivateUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Set deactivate user */
        var deactivateWriterReq = UserReqDto.builder()
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
        var deactivateWriterRsp = userService.signUp(deactivateWriterReq);
        assertThat(deactivateWriterRsp).isNotNull();
        assertThat(deactivateWriterRsp.getUserId()).isNotNull();

        userService.delete(deactivateWriterRsp.getUserId());

        /* 0-3. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(deactivateWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        assertThrows(
                MemoryDeactivateWriterException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 추가 -> 개인방 | 성공")
    void insertMemoryInPrivateRoomSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertWriterRsp.getPrivateRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory to private room */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
    }

    @Test
    @DisplayName("일정 추가 -> 개인방 | 실패 | 잘못된 사용자번호")
    void insertMemoryInPrivateRoomFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId() + 50000)
                .roomId(insertWriterRsp.getPrivateRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory to private room */
        assertThrows(
                MemoryNotFoundWriterException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 추가 -> 개인방 | 실패 | 탈퇴한 일정 작성자번호")
    void insertMemoryInPrivateRoomFailToDeactivateUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Set deactivate user */
        var deactivateWriterReq = UserReqDto.builder()
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
        var deactivateWriterRsp = userService.signUp(deactivateWriterReq);
        assertThat(deactivateWriterRsp).isNotNull();
        assertThat(deactivateWriterRsp.getUserId()).isNotNull();

        userService.delete(deactivateWriterRsp.getUserId());

        /* 0-3. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(deactivateWriterRsp.getUserId())
                .roomId(insertWriterRsp.getPrivateRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory to private room */
        assertThrows(
                MemoryDeactivateWriterException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 추가 -> 개인방 | 실패 | 잘못된 방번호")
    void insertMemoryInPrivateRoomFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertWriterRsp.getPrivateRoomId() + 50000)
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory to private room */
        assertThrows(
                MemoryNotFoundRoomException.class, () -> memoryService.insert(insertMemoryReq)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 공유방 일정 | 성공")
    void findShareMemorySuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from inserted room */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId()
        );
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 개별 조회 -> 공유방 일정 | 실패 | 잘못된 일정번호")
    void findShareMemoryFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from inserted room */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 5000;
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(wrongMemoryId, roomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 공유방 일정 | 실패 | 삭제된 일정번호")
    void findShareMemoryFailToDeletedMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Delete memory */
        var deleteMemoryRsp = memoryService.delete(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getWriterId(), insertWriterRsp.getPrivateRoomId()
        );
        assertNull(deleteMemoryRsp);

        /* 3. Find Memory from inserted room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId, roomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 공유방 일정 | 실패 | 잘못된 방번호")
    void findShareMemoryFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from inserted room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var wrongRoomId = insertRoomRsp.getRoomId() + 5000;
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.find(memoryId, wrongRoomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 공유방 일정 | 실패 | 삭제된 방번호")
    void findShareMemoryFailToDeletedRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Delete room */
        var deleteRoomRsp = roomService.delete(
                insertMemoryRsp.getAddedRoomId(), insertMemoryRsp.getWriterId()
        );
        assertNull(deleteRoomRsp);

        /* 3. Find Memory from inserted room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.find(memoryId, roomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 공유방 일정 | 실패 | 방에 포함되지 않은 일정")
    void findShareMemoryFailToNotIncludeRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertWriterRsp.getUserId())
                .opened(false)
                .member(List.of(insertMemberRsp.getUserId()))
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers()).isNotNull();
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from inserted room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var notIncludeRoomId = insertRoomRsp2.getRoomId();
        assertThrows(
                MemoryNotIncludeRoomException.class, () -> memoryService.find(memoryId, notIncludeRoomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 개인방 일정 | 성공")
    void findPrivateMemorySuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from private room */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertWriterRsp.getPrivateRoomId()
        );
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 개별 조회 -> 개인방 일정 | 실패 | 잘못된 일정번호")
    void findPrivateMemoryFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from private room */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 5000;
        var roomId = insertWriterRsp.getPrivateRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(wrongMemoryId, roomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 개인방 일정 | 실패 | 삭제된 일정번호")
    void findPrivateMemoryFailToDeletedMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Delete memory */
        var deleteMemoryRsp = memoryService.delete(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getWriterId(), insertMemoryRsp.getAddedRoomId()
        );
        assertNull(deleteMemoryRsp);

        /* 3. Find Memory from private room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var roomId = insertWriterRsp.getPrivateRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId, roomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 개인방 일정 | 실패 | 잘못된 방번호")
    void findPrivateMemoryFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from private room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var wrongRoomId = insertWriterRsp.getPrivateRoomId() + 5000;
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.find(memoryId, wrongRoomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 개인방 일정 | 실패 | 삭제된 방번호")
    void findPrivateMemoryFailToDeletedRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Delete room */
        var deleteRoomRsp = roomService.delete(
                insertMemoryRsp.getAddedRoomId(), insertMemoryRsp.getWriterId()
        );
        assertNull(deleteRoomRsp);

        /* 3. Find Memory from private room -> room & memory deleted. */
        var memoryId = insertMemoryRsp.getMemoryId();
        var roomId = insertWriterRsp.getPrivateRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.find(memoryId, roomId)
        );
    }

    @Test
    @DisplayName("일정 개별 조회 -> 개인방 일정 | 실패 | 방에 포함되지 않은 일정")
    void findPrivateMemoryFailToNotIncludeRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .name("Test Memory")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertWriterRsp.getUserId())
                .opened(false)
                .member(List.of(insertMemberRsp.getUserId()))
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers()).isNotNull();
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertWriterRsp.getPrivateRoomId());
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryReq.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryReq.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryReq.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryReq.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryReq.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryReq.getFirstAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryReq.getBgColor());

        /* 2. Find Memory from private room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var notIncludeRoomId = insertRoomRsp2.getRoomId();
        assertThrows(
                MemoryNotIncludeRoomException.class, () -> memoryService.find(memoryId, notIncludeRoomId)
        );
    }

    @Test
    @DisplayName("일정 참석 | 성공")
    void attendMemorySuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        var attendMemoryReq = MemoryReqDto.builder()
                .userId(insertMemberRsp.getUserId())
                .attendanceStatus(AttendanceStatus.ATTEND)
                .build();

        var attendRsp = memoryService.setAttendanceStatus(insertMemoryRsp.getMemoryId(), attendMemoryReq);
        assertThat(attendRsp).isNotNull();
        assertThat(attendRsp.getUserAttendances().size()).isOne();
    }

    @Test
    @DisplayName("일정 참석 | 실패 | 잘못된 사용자번호")
    void attendMemoryFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        var wrongAttendMemoryReq = MemoryReqDto.builder()
                .userId(insertMemberRsp.getUserId() + 50000)
                .attendanceStatus(AttendanceStatus.ATTEND)
                .build();
        var memoryId = insertMemoryRsp.getMemoryId();

        assertThrows(
                UserNotFoundException.class, () -> memoryService.setAttendanceStatus(memoryId, wrongAttendMemoryReq)
        );

        /* 4. Find memory after attend memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isZero();
    }

    @Test
    @DisplayName("일정 참석 | 실패 | 잘못된 일정번호")
    void attendMemoryFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        var attendMemoryReq = MemoryReqDto.builder()
                .userId(insertMemberRsp.getUserId())
                .attendanceStatus(AttendanceStatus.ATTEND)
                .build();
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 50000;

        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.setAttendanceStatus(wrongMemoryId, attendMemoryReq)
        );

        /* 4. Find memory after attend memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isZero();
    }

    @Test
    @DisplayName("일정 불참 | 성공")
    void absentMemorySuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        var attendMemoryReq = MemoryReqDto.builder()
                .userId(insertMemberRsp.getUserId())
                .attendanceStatus(AttendanceStatus.ABSENCE)
                .build();
        var attendMemoryRsp = memoryService.setAttendanceStatus(
                insertMemoryRsp.getMemoryId(), attendMemoryReq
        );
        assertThat(attendMemoryRsp).isNotNull();
        assertThat(attendMemoryRsp.getUserAttendances().size()).isOne();

        /* 4. Find memory after absence memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isOne();
    }

    @Test
    @DisplayName("일정 불참 | 실패 | 잘못된 사용자번호")
    void absentMemoryFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        var wrongAttendMemoryReq = MemoryReqDto.builder()
                .userId(insertMemberRsp.getUserId() + 50000)
                .attendanceStatus(AttendanceStatus.ABSENCE)
                .build();
        var memoryId = insertMemoryRsp.getMemoryId();

        assertThrows(
                UserNotFoundException.class, () -> memoryService.setAttendanceStatus(memoryId, wrongAttendMemoryReq)
        );

        /* 4. Find memory after absence memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isZero();
    }

    @Test
    @DisplayName("일정 불참 | 실패 | 잘못된 일정번호")
    void absentMemoryFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        var attendMemoryReq = MemoryReqDto.builder()
                .userId(insertMemberRsp.getUserId())
                .attendanceStatus(AttendanceStatus.ABSENCE)
                .build();
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 50000;

        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.setAttendanceStatus(wrongMemoryId, attendMemoryReq)
        );

        /* 4. Find memory after absence memory and check attendance status */
        var afterFindMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId());
        assertThat(afterFindMemoryRsp).isNotNull();
        assertThat(afterFindMemoryRsp.getUserAttendances().size()).isZero();
    }

    @Test
    @DisplayName("일정 공유 -> 개별 사용자 목록 | 성공")
    void shareMemoryForUsersSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()))
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
            var memoryRsp = findMemberRoomRsp.getMemories().get(0);
            assertThat(memoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
            isValidMember = true;
            memberRoomId = findMemberRoomRsp.getRoomId();
        }
        assertTrue(isValidMember);

        // 2) Check from member2
        var findMemberRooms2 = roomService.findRooms(insertMemberRsp2.getUserId(), null);
        assertThat(findMemberRooms2.size()).isOne();

        var findMemberRoomRsp2 = findMemberRooms2.get(0);
        assertThat(findMemberRoomRsp2.getMemories().size()).isOne();
        assertThat(findMemberRoomRsp2.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Check shared memory in each rooms
        assertNotEquals(findMemberRoomRsp2.getRoomId(), memberRoomId);
    }

    @Test
    @DisplayName("일정 공유 -> 개별 사용자 목록 | 실패 | 잘못된 공유자번호")
    void shareMemoryForUsersFailToWrongSharerId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for users */
        var memoryId = insertMemoryRsp.getMemoryId();
        var wrongSharerId = insertWriterRsp.getUserId() + 50000;

        assertThrows(
                UserNotFoundException.class, () -> memoryService.shareMemory(memoryId, wrongSharerId, shareMemoryUsersReq)
        );
    }

    @Test
    @DisplayName("일정 공유 -> 개별 사용자 목록 | 실패 | 잘못된 일정번호")
    void shareMemoryForUsersFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for users */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 50000;
        var sharerId = insertWriterRsp.getUserId();

        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.shareMemory(wrongMemoryId, sharerId, shareMemoryUsersReq)
        );
    }

    @Test
    @DisplayName("일정 공유 -> 개별 사용자 목록 | 실패 | 잘못된 공유대상 사용자번호")
    void shareMemoryForUsersFailToWrongTargetUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId() + 5000))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for users */
        var memoryId = insertMemoryRsp.getMemoryId();
        var sharerId = insertWriterRsp.getUserId();

        assertThrows(
                MemoryNotFoundShareMemberException.class,
                () -> memoryService.shareMemory(memoryId, sharerId, shareMemoryUsersReq)
        );

        /* 3. Check rollback share memory from targets */
        var findRooms = roomService.findRooms(insertMemberRsp.getUserId(), null);
        assertThat(findRooms.size()).isOne();
    }

    @Test
    @DisplayName("일정 공유 -> 사용자 그룹 | 성공")
    void shareMemoryForUserGroupSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()))
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
        assertThat(shareMemoryRsp.getShareRooms().size()).isEqualTo(2);

        /* 3. find share memory */
        // 1) Check from member1
        var findMemberRooms = roomService.findRooms(insertMemberRsp.getUserId(), null);
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
            assertThat(findMemberRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
            isValidMember = true;
            memberRoomId = findMemberRoomRsp.getRoomId();
        }
        assertTrue(isValidMember);

        // 2) Check from member2
        var findMemberRooms2 = roomService.findRooms(insertMemberRsp2.getUserId(), null);
        assertThat(findMemberRooms2.size()).isOne();

        var findMemberRoomRsp2 = findMemberRooms2.get(0);
        assertThat(findMemberRoomRsp2.getMemories().size()).isOne();
        assertThat(findMemberRoomRsp2.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Check shared memory in same room
        assertEquals(memberRoomId, findMemberRoomRsp2.getRoomId());
    }

    @Test
    @DisplayName("일정 공유 -> 사용자 그룹 | 실패 | 잘못된 공유자번호")
    void shareMemoryForUserGroupFailToWrongSharerId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for user group */
        var memoryId = insertMemoryRsp.getMemoryId();
        var wrongSharerId = insertWriterRsp.getUserId() + 50000;
        assertThrows(
                UserNotFoundException.class, () -> memoryService.shareMemory(
                        memoryId, wrongSharerId, shareMemoryUsersReq
                )
        );
    }

    @Test
    @DisplayName("일정 공유 -> 사용자 그룹 | 실패 | 잘못된 일정번호")
    void shareMemoryForUserGroupFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for user group */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 50000;
        var sharerId = insertWriterRsp.getUserId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.shareMemory(
                        wrongMemoryId, sharerId, shareMemoryUsersReq
                )
        );
    }

    @Test
    @DisplayName("일정 공유 -> 사용자 그룹 | 실패 | 잘못된 공유대상 사용자번호")
    void shareMemoryForUserGroupFailToWrongTargetUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
                .shareIds(List.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId() + 500))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for user group */
        var memoryId = insertMemoryRsp.getMemoryId();
        var sharerId = insertWriterRsp.getUserId();
        assertThrows(
                MemoryNotFoundShareMemberException.class, () -> memoryService.shareMemory(
                        memoryId, sharerId, shareMemoryUsersReq
                )
        );
    }

    @Test
    @DisplayName("일정 공유 -> 방 목록 | 성공")
    void shareMemoryForRoomsSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var members2 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq3 = RoomReqDto.builder()
                .name("room name3")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members3)
                .build();
        var insertRoomRsp3 = roomService.insert(insertRoomReq3);
        assertThat(insertRoomRsp3.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp3.getMembers().size()).isEqualTo(2);

        var shareMemoryUsersReq = MemoryReqDto.builder()
                .shareType(ShareType.ROOMS)
                .shareIds(List.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for rooms */
        var shareMemoryRsp = memoryService.shareMemory(
                insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), shareMemoryUsersReq
        );
        assertThat(shareMemoryRsp.getShareRooms().size()).isEqualTo(3);

        /* 3. Check share memory from rooms */
        var findRoom2Rsp = roomService.find(insertRoomRsp2.getRoomId());
        assertThat(findRoom2Rsp.getMemories().size()).isOne();
        assertThat(findRoom2Rsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        var findRoom3Rsp = roomService.find(insertRoomRsp3.getRoomId());
        assertThat(findRoom3Rsp.getMemories().size()).isOne();
        assertThat(findRoom3Rsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 공유 -> 방 목록 | 실패 | 잘못된 공유자번호")
    void shareMemoryForRoomsFailToWrongSharerId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var members2 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq3 = RoomReqDto.builder()
                .name("room name3")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members3)
                .build();
        var insertRoomRsp3 = roomService.insert(insertRoomReq3);
        assertThat(insertRoomRsp3.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp3.getMembers().size()).isEqualTo(2);

        var shareMemoryUsersReq = MemoryReqDto.builder()
                .shareType(ShareType.ROOMS)
                .shareIds(List.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for rooms */
        var memoryId = insertMemoryRsp.getMemoryId();
        var wrongSharerId = insertWriterRsp.getUserId() + 5000;
        assertThrows(
                UserNotFoundException.class,
                () -> memoryService.shareMemory(memoryId, wrongSharerId, shareMemoryUsersReq)
        );

        /* 3. Check not share memory from rooms */
        var findRoom2Rsp = roomService.find(insertRoomRsp2.getRoomId());
        assertThat(findRoom2Rsp.getMemories().size()).isZero();

        var findRoom3Rsp = roomService.find(insertRoomRsp3.getRoomId());
        assertThat(findRoom3Rsp.getMemories().size()).isZero();
    }

    @Test
    @DisplayName("일정 공유 -> 방 목록 | 실패 | 잘못된 일정번호")
    void shareMemoryForRoomsFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var members2 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq3 = RoomReqDto.builder()
                .name("room name3")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members3)
                .build();
        var insertRoomRsp3 = roomService.insert(insertRoomReq3);
        assertThat(insertRoomRsp3.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp3.getMembers().size()).isEqualTo(2);

        var shareMemoryUsersReq = MemoryReqDto.builder()
                .shareType(ShareType.ROOMS)
                .shareIds(List.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for rooms */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 5000;
        var sharerId = insertWriterRsp.getUserId();
        assertThrows(
                MemoryNotFoundException.class,
                () -> memoryService.shareMemory(wrongMemoryId, sharerId, shareMemoryUsersReq)
        );

        /* 3. Check not share memory from rooms */
        var findRoom2Rsp = roomService.find(insertRoomRsp2.getRoomId());
        assertThat(findRoom2Rsp.getMemories().size()).isZero();

        var findRoom3Rsp = roomService.find(insertRoomRsp3.getRoomId());
        assertThat(findRoom3Rsp.getMemories().size()).isZero();
    }

    @Test
    @DisplayName("일정 공유 -> 방 목록 | 실패 | 잘못된 공유대상 방번호")
    void shareMemoryForRoomsFailToWrongTargetRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var members2 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = List.of(insertWriterRsp.getUserId());
        var insertRoomReq3 = RoomReqDto.builder()
                .name("room name3")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members3)
                .build();
        var insertRoomRsp3 = roomService.insert(insertRoomReq3);
        assertThat(insertRoomRsp3.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp3.getMembers().size()).isEqualTo(2);

        var shareMemoryUsersReq = MemoryReqDto.builder()
                .shareType(ShareType.ROOMS)
                .shareIds(List.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId() + 500))
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Share memory for rooms */
        var memoryId = insertMemoryRsp.getMemoryId();
        var sharerId = insertWriterRsp.getUserId();
        assertThrows(
                MemoryNotFoundShareRoomException.class,
                () -> memoryService.shareMemory(memoryId, sharerId, shareMemoryUsersReq)
        );

        /* 3. Check not share memory from rooms */
        var findRoom2Rsp = roomService.find(insertRoomRsp2.getRoomId());
        assertThat(findRoom2Rsp.getMemories().size()).isZero();

        var findRoom3Rsp = roomService.find(insertRoomRsp3.getRoomId());
        assertThat(findRoom3Rsp.getMemories().size()).isZero();
    }

    @Test
    @DisplayName("일정 삭제 -> 공유방 | 성공")
    void deleteMemoryFromShareRoomSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from share room */
        var deleteRsp = memoryService.delete(
                insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), insertRoomRsp.getRoomId()
        );
        assertNull(deleteRsp);

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var memoryId = insertMemoryRsp.getMemoryId();
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(MemoryNotIncludeRoomException.class, () -> memoryService.find(memoryId, roomId));

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().size()).isZero();
    }

    @Test
    @DisplayName("일정 삭제 -> 공유방 | 실패 | 잘못된 사용자번호")
    void deleteMemoryFromShareRoomFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from share room */
        var deleteMemoryId = insertMemoryRsp.getMemoryId();
        var wrongWriterId = insertWriterRsp.getUserId() + 5000;
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(
                UserNotFoundException.class, () -> memoryService.delete(deleteMemoryId, wrongWriterId, roomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 삭제 -> 공유방 | 실패 | 잘못된 일정번호")
    void deleteMemoryFromShareRoomFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from share room */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 5000;
        var writerId = insertWriterRsp.getUserId();
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.delete(wrongMemoryId, writerId, roomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 삭제 -> 공유방 | 실패 | 잘못된 방번호")
    void deleteMemoryFromShareRoomFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from share room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var writerId = insertWriterRsp.getUserId();
        var wrongRoomId = insertRoomRsp.getRoomId() + 5000;
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.delete(memoryId, writerId, wrongRoomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 삭제 -> 공유방 | 실패 | 방에 포함되지 않은 일정")
    void deleteMemoryFromShareRoomFailToNotIncludeRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertWriterRsp.getUserId())
                .opened(false)
                .member(List.of(insertMemberRsp.getUserId()))
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers()).isNotNull();
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp).isNotNull();
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from share room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var writerId = insertWriterRsp.getUserId();
        var notIncludeRoomId = insertRoomRsp2.getRoomId();
        assertThrows(
                MemoryNotIncludeRoomException.class, () -> memoryService.delete(memoryId, writerId, notIncludeRoomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 삭제 -> 개인방 | 성공")
    void deleteMemoryFromPrivateRoomSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from private room */
        var deleteRsp = memoryService.delete(
                insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), insertWriterRsp.getPrivateRoomId()
        );
        assertNull(deleteRsp);

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isZero();

        // 2) Find memory from inserted room
        var memoryId = insertMemoryRsp.getMemoryId();
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(MemoryNotFoundException.class, () -> memoryService.find(memoryId, roomId));

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isZero();

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().size()).isZero();
    }

    @Test
    @DisplayName("일정 삭제 -> 개인방 | 실패 | 잘못된 사용자번호")
    void deleteMemoryFromPrivateRoomFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from private room */
        var deleteMemoryId = insertMemoryRsp.getMemoryId();
        var wrongWriterId = insertWriterRsp.getUserId() + 5000;
        var privateRoomId = insertWriterRsp.getPrivateRoomId();
        assertThrows(
                UserNotFoundException.class, () -> memoryService.delete(deleteMemoryId, wrongWriterId, privateRoomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 삭제 -> 개인방 | 실패 | 잘못된 일정번호")
    void deleteMemoryFromPrivateRoomFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from private room */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 5000;
        var writerId = insertWriterRsp.getUserId();
        var privateRoomId = insertWriterRsp.getPrivateRoomId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.delete(wrongMemoryId, writerId, privateRoomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 삭제 -> 개인방 | 실패 | 잘못된 방번호")
    void deleteMemoryFromPrivateRoomFailToWrongRoomId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from private room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var writerId = insertWriterRsp.getUserId();
        var wrongPrivateRoomId = insertWriterRsp.getPrivateRoomId() + 5000;
        assertThrows(
                RoomNotFoundException.class, () -> memoryService.delete(memoryId, writerId, wrongPrivateRoomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 삭제 -> 개인방 | 실패 | 방에 포함되지 않은 일정")
    void deleteMemoryFromPrivateRoomFailToNotIncludeRoom() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertWriterRsp.getUserId())
                .opened(false)
                .member(List.of(insertMemberRsp.getUserId()))
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers()).isNotNull();
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getWriterId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertMemoryRsp.getAddedRoomId()).isEqualTo(insertMemoryReq.getRoomId());

        /* 2. Delete memory from private room */
        var memoryId = insertMemoryRsp.getMemoryId();
        var writerId = insertWriterRsp.getUserId();
        var notIncludeRoomId = insertRoomRsp2.getRoomId();
        assertThrows(
                MemoryNotIncludeRoomException.class, () -> memoryService.delete(memoryId, writerId, notIncludeRoomId)
        );

        /* 3. Check delete memory */
        // 1) Find memory from writer
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null, null, null);
        assertThat(findMemoriesRsp.size()).isOne();
        assertThat(findMemoriesRsp.get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 2) Find memory from inserted room
        var findMemoryRsp = memoryService.find(insertMemoryRsp.getMemoryId(), insertRoomRsp.getRoomId());
        assertThat(findMemoryRsp.getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 3) Find memory from private room
        var findPrivateRoomRsp = roomService.find(insertWriterRsp.getPrivateRoomId());
        assertThat(findPrivateRoomRsp.getMemories().size()).isOne();
        assertThat(findPrivateRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());

        // 4) Find memory from share room
        var findShareRoomRsp = roomService.find(insertRoomRsp.getRoomId());
        assertThat(findShareRoomRsp.getMemories().get(0).getMemoryId()).isEqualTo(insertMemoryRsp.getMemoryId());
    }

    @Test
    @DisplayName("일정 수정 -> 작성자 | 성공")
    void updateMemoryByWriterSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF") // 배경색
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .secondAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 두 번째 알림
                .bgColor("#000033") // 배경색
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(insertMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());

        /* 2. Update */
        var updateMemoryRsp = memoryService.update(insertMemoryRsp.getMemoryId(), insertWriterRsp.getUserId(), updateReq);
        assertThat(updateMemoryRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(updateMemoryRsp.getContents()).isEqualTo(updateReq.getContents());
        assertThat(updateMemoryRsp.getPlace()).isEqualTo(updateReq.getPlace());
        assertThat(updateMemoryRsp.getStartDate()).isEqualTo(updateReq.getStartDate());
        assertThat(updateMemoryRsp.getEndDate()).isEqualTo(updateReq.getEndDate());
        assertThat(updateMemoryRsp.getFirstAlarm()).isEqualTo(updateReq.getFirstAlarm());
        assertThat(updateMemoryRsp.getSecondAlarm()).isEqualTo(updateReq.getSecondAlarm());
        assertThat(updateMemoryRsp.getBgColor()).isEqualTo(updateReq.getBgColor());

        /* 3. Check update to compare before */
        assertNotEquals(updateMemoryRsp.getName(), insertMemoryRsp.getName());
        assertNotEquals(updateMemoryRsp.getContents(), insertMemoryRsp.getContents());
        assertNotEquals(updateMemoryRsp.getPlace(), insertMemoryRsp.getPlace());
        assertNotEquals(updateMemoryRsp.getStartDate(), insertMemoryRsp.getStartDate());
        assertNotEquals(updateMemoryRsp.getEndDate(), insertMemoryRsp.getEndDate());
        assertNotEquals(updateMemoryRsp.getFirstAlarm(), insertMemoryRsp.getFirstAlarm());
        assertNotEquals(updateMemoryRsp.getSecondAlarm(), insertMemoryRsp.getSecondAlarm());
        assertNotEquals(updateMemoryRsp.getBgColor(), insertMemoryRsp.getBgColor());
    }

    @Test
    @DisplayName("일정 수정 -> 작성자 | 실패 | 잘못된 사용자번호")
    void updateMemoryByWriterFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF") // 배경색
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .secondAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 두 번째 알림
                .bgColor("#000033") // 배경색
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(insertMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());

        /* 2. Update */
        var memoryId = insertMemoryRsp.getMemoryId();
        var wrongWriterId = insertWriterRsp.getUserId() + 5000;
        assertThrows(
                UserNotFoundException.class, () -> memoryService.update(memoryId, wrongWriterId, updateReq)
        );

        /* 3. Check not update */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId()
        );
        assertThat(findMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(findMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(findMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(findMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(findMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(findMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(findMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(findMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());
    }

    @Test
    @DisplayName("일정 수정 -> 작성자 | 실패 | 잘못된 일정번호")
    void updateMemoryByWriterFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF") // 배경색
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .secondAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 두 번째 알림
                .bgColor("#000033") // 배경색
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(insertMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());

        /* 2. Update */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 5000;
        var writerId = insertWriterRsp.getUserId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.update(wrongMemoryId, writerId, updateReq)
        );

        /* 3. Check not update */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId()
        );
        assertThat(findMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(findMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(findMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(findMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(findMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(findMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(findMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(findMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());
    }

    @Test
    @DisplayName("일정 수정 -> 작성자 | 실패 | 일정 작성자가 아닌 경우")
    void updateMemoryByWriterFailToNotMatchedWriterId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF") // 배경색
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .secondAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 두 번째 알림
                .bgColor("#000033") // 배경색
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(insertMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());

        /* 2. Update */
        var memoryId = insertMemoryRsp.getMemoryId();
        var memberId = insertMemberRsp.getUserId();
        assertThrows(
                MemoryNotWriterException.class, () -> memoryService.update(memoryId, memberId, updateReq)
        );

        /* 3. Check not update */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId()
        );
        assertThat(findMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(findMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(findMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(findMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(findMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(findMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(findMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(findMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());
    }

    @Test
    @DisplayName("일정 수정 -> 작성자 외 다른 사람 | 성공")
    void updateMemoryByOtherSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(insertMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());

        /* 2. Update */
        var memoryId = insertMemoryRsp.getMemoryId();
        var memberId = insertMemberRsp.getUserId();
        assertThrows(
                MemoryNotWriterException.class, () -> memoryService.update(memoryId, memberId, updateReq)
        );

        /* 3. Check not update */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId()
        );
        assertThat(findMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(findMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(findMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(findMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(findMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(findMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(findMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(findMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());
    }

    @Test
    @DisplayName("일정 수정 -> 작성자 외 다른 사람 | 실패 | 잘못된 사용자번호")
    void updateMemoryByOtherFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(insertMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());

        /* 2. Update */
        var memoryId = insertMemoryRsp.getMemoryId();
        var wrongMemberId = insertMemberRsp.getUserId() + 5000;
        assertThrows(
                UserNotFoundException.class, () -> memoryService.update(memoryId, wrongMemberId, updateReq)
        );

        /* 3. Check not update */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId()
        );
        assertThat(findMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(findMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(findMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(findMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(findMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(findMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(findMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(findMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());
    }

    @Test
    @DisplayName("일정 수정 -> 작성자 외 다른 사람 | 실패 | 잘못된 일정번호")
    void updateMemoryByOtherFailToWrongMemoryId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
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
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var updateReq = MemoryReqDto.builder()
                .name("Update memory name")
                .contents("Update contents")
                .place("Update place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .build();

        /* 1. Make memory */
        var insertMemoryRsp = memoryService.insert(insertMemoryReq);
        assertThat(insertMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(insertMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(insertMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(insertMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(insertMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(insertMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(insertMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(insertMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());

        /* 2. Update */
        var wrongMemoryId = insertMemoryRsp.getMemoryId() + 5000;
        var memberId = insertMemberRsp.getUserId();
        assertThrows(
                MemoryNotFoundException.class, () -> memoryService.update(wrongMemoryId, memberId, updateReq)
        );

        /* 3. Check not update */
        var findMemoryRsp = memoryService.find(
                insertMemoryRsp.getMemoryId(), insertMemoryRsp.getAddedRoomId()
        );
        assertThat(findMemoryRsp.getName()).isEqualTo(insertMemoryRsp.getName());
        assertThat(findMemoryRsp.getContents()).isEqualTo(insertMemoryRsp.getContents());
        assertThat(findMemoryRsp.getPlace()).isEqualTo(insertMemoryRsp.getPlace());
        assertThat(findMemoryRsp.getStartDate()).isEqualTo(insertMemoryRsp.getStartDate());
        assertThat(findMemoryRsp.getEndDate()).isEqualTo(insertMemoryRsp.getEndDate());
        assertThat(findMemoryRsp.getFirstAlarm()).isEqualTo(insertMemoryRsp.getFirstAlarm());
        assertThat(findMemoryRsp.getSecondAlarm()).isEqualTo(insertMemoryRsp.getSecondAlarm());
        assertThat(findMemoryRsp.getBgColor()).isEqualTo(insertMemoryRsp.getBgColor());
    }

    @Test
    @DisplayName("일정 목록 조회 -> 전체기간 | 성공")
    void findAllMemoriesSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertMemoryReq1 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory1")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(7).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(7).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq2 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory2")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(8).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq3 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory3")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(4).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq4 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory4")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(4).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq5 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory1")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(4).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(4).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq6 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory6")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(7).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var insertMemoryReq7 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory7")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(9).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(9).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        List<MemoryRspDto> findMemoriesList = memoryService.findMemories(insertMemoryReq1.getUserId(), null, null, null);
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

    @Test
    @DisplayName("일정 목록 조회 -> 월 범위 | 성공")
    void findMonthsMemoriesSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var now = LocalDateTime.now();

        // 1) -2 Month last Day 00H:00M ~ -2 Month last Day 23H:59M | Edge case, Out of range
        var insertMemoryReq1 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory1")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.minusMonths(2).getYear(),
                                now.minusMonths(2).getMonthValue(),
                                now.minusMonths(2).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth(),
                                0,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.minusMonths(2).getYear(),
                                now.minusMonths(2).getMonthValue(),
                                now.minusMonths(2).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth(),
                                23,
                                59).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().minusMonths(2).plusDays(6).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        // 2) -1 Month 1 Day 00H:00M ~ -1 Month 8 Day 14H:00M
        var insertMemoryReq2 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory3")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.minusMonths(1).getYear(),
                                now.minusMonths(1).getMonthValue(),
                                1,
                                0,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.minusMonths(1).getYear(),
                                now.minusMonths(1).getMonthValue(),
                                8,
                                14,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().minusMonths(2).plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        // 3) -2 Month 20 Day 00H:00M ~ -1 Month 1 Day 00H:00M | Edge case
        var insertMemoryReq3 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory2")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.minusMonths(2).getYear(),
                                now.minusMonths(2).getMonthValue(),
                                20,
                                0,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.minusMonths(1).getYear(),
                                now.minusMonths(1).getMonthValue(),
                                1,
                                0,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().minusMonths(1).plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        // 4) now Month now Day 00H:00M ~ now Month now Day 01H:00M | Same case to 5), Early insert
        var insertMemoryReq4 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory4")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.getYear(),
                                now.getMonthValue(),
                                now.getDayOfMonth(),
                                0,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.getYear(),
                                now.getMonthValue(),
                                now.getDayOfMonth(),
                                1,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(4).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        // 5) now Month now Day 00H:00M ~ now Month now Day 01H:00M | Same case to 4), Rate insert
        var insertMemoryReq5 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory1")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                                LocalDateTime.of(
                                        now.getYear(),
                                        now.getMonthValue(),
                                        now.getDayOfMonth(),
                                        0,
                                        0).format(alertTimeFormat),
                                alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                                LocalDateTime.of(
                                        now.getYear(),
                                        now.getMonthValue(),
                                        now.getDayOfMonth(),
                                        1,
                                        0).format(alertTimeFormat),
                                alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusMonths(1).plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        // 6) +2 Month 1 Day 00H:00M ~ +2 Month 1 Day 01H:00M | Edge case, Out of range
        var insertMemoryReq6 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory6")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.plusMonths(2).getYear(),
                                now.plusMonths(2).getMonthValue(),
                                1,
                                0,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.plusMonths(2).getYear(),
                                now.plusMonths(2).getMonthValue(),
                                1,
                                1,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusMonths(1).plusDays(3).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        // 7) +1 Month last Day 23H:59M ~ +2 Month 1 Day 06H:00M | Edge case
        var insertMemoryReq7 = MemoryReqDto.builder()
                .userId(insertWriterRsp.getUserId())
                .roomId(insertRoomRsp.getRoomId())
                .name("Test Memory7")
                .contents("Test Contents")
                .place("Test Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.plusMonths(1).getYear(),
                                now.plusMonths(1).getMonthValue(),
                                now.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth(),
                                23,
                                59).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.of(
                                now.plusMonths(2).getYear(),
                                now.plusMonths(2).getMonthValue(),
                                1,
                                6,
                                0).format(alertTimeFormat),
                        alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusMonths(1).plusDays(2).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
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
        var startMonth = YearMonth.parse(
                YearMonth.now().minusMonths(1).format(dateFormat), dateFormat);
        var endMonth = YearMonth.parse(
                YearMonth.now().plusMonths(1).format(dateFormat), dateFormat);
        var findMemoriesList = memoryService.findMemories(
                insertMemoryReq1.getUserId(), null, startMonth, endMonth
        );
        assertThat(findMemoriesList.size()).isEqualTo(5);

        // Expected order: 3 2 4 5 7(Out of range: 1, 6)
        var findMemoriesRsp1 = findMemoriesList.get(0);
        assertThat(findMemoriesRsp1.getMemoryId()).isEqualTo(insertMemoryRsp3.getMemoryId());

        var findMemoriesRsp2 = findMemoriesList.get(1);
        assertThat(findMemoriesRsp2.getMemoryId()).isEqualTo(insertMemoryRsp2.getMemoryId());

        var findMemoriesRsp3 = findMemoriesList.get(2);
        assertThat(findMemoriesRsp3.getMemoryId()).isEqualTo(insertMemoryRsp4.getMemoryId());

        var findMemoriesRsp4 = findMemoriesList.get(3);
        assertThat(findMemoriesRsp4.getMemoryId()).isEqualTo(insertMemoryRsp5.getMemoryId());

        var findMemoriesRsp5 = findMemoriesList.get(4);
        assertThat(findMemoriesRsp5.getMemoryId()).isEqualTo(insertMemoryRsp7.getMemoryId());
    }

    // life cycle: @Before -> @Test => separate => Not maintained 
    // Call function in @Test function => maintained 
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
        assertThat(insertMemberRsp.getUserId()).isNotNull();

        /* 2. Create room */
        var members = List.of(insertMemberRsp.getUserId());
        var insertRoomReq = RoomReqDto.builder()
                .name("room name")
                .userId(insertWriterRsp.getUserId())
                .opened(false)
                .member(members)
                .build();
        insertRoomRsp = roomService.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(2);
    }

}
