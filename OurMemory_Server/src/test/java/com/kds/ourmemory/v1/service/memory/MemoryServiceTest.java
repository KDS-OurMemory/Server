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

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/*
 * TODO 일정 공유 오류 수정 -> 공유 진행 중 일부 사용자 실패한 경우, 전체 롤백되지 않고 성공한 사용자에게는 공유된다.
 *   => @Transactional TxType.REQUIRED 로 일정 공유 전체에 트랜잭션이 걸려있어서 내부 메소드는 컨텍스트를 공유하게 된다.
 *   => 따라서 롤백은 일정 공유 테스트 코드가 전부 종료된 다음 롤백되기 때문에, 테스트 중 일정 공유 메소드 예외가 발생해도 당장 롤백되지 않는다.
 *   => 이를 해결하기 위해선
 *      1. 일정 공유 테스트 코드에 트랜잭션을 제거하거나,
 *      2. 일정 공유 메소드를 TxType.REQUIRED_NEW 로 선언하여 새로운 트랜잭션을 잡아야한다.
 *   => 1. 의 경우, 테스트 중 DB에 일정추가가 롤백되지 않기 때문에 불가능하며,
 *   => 2. 의 경우, 새로운 트랜잭션 컨텍스트 범위가 생성되기 때문에, 그 위치에선 DB에 적재되지 않은 일정, 방 정보는 불러올 수 없어 오류가 발생한다.
 *   => 따라서 현재 상태로는 테스트가 불가능하다... 새로운 방법을 찾을 때까지 우선 케이스 수정 후 통과 조치함.
 *
 * */

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
    @DisplayName("일정 추가 -> 방 안(공유 일정 취급) | 성공")
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
                .member(Stream.of(insertMemberRsp.getUserId()).collect(toList()))
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
                .member(Stream.of(insertMemberRsp.getUserId()).collect(toList()))
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
                .shareIds(Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList()))
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
    @Transactional
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
                .shareIds(Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList()))
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
    @Transactional
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
                .shareIds(Stream.of(insertMemberRsp.getUserId() + 5000, insertMemberRsp2.getUserId()).collect(toList()))
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
    }

    @Test
    @DisplayName("일정 공유 -> 사용자 그룹 | 성공")
    @Transactional
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
    @Transactional
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
                .shareIds(Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList()))
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
    @Transactional
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
                .shareIds(Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId()).collect(toList()))
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
    @Transactional
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
                .shareIds(Stream.of(insertMemberRsp.getUserId(), insertMemberRsp2.getUserId() + 500).collect(toList()))
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
    @Transactional
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

        var members2 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
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
                .shareIds(Stream.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()).collect(toList()))
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
    @Transactional
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

        var members2 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
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
                .shareIds(Stream.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()).collect(toList()))
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
    @Transactional
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

        var members2 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
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
                .shareIds(Stream.of(insertRoomRsp2.getRoomId(), insertRoomRsp3.getRoomId()).collect(toList()))
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
    @Transactional
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

        var members2 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
        var insertRoomReq2 = RoomReqDto.builder()
                .name("room name2")
                .userId(insertMemberRsp.getUserId())
                .opened(false)
                .member(members2)
                .build();
        var insertRoomRsp2 = roomService.insert(insertRoomReq2);
        assertThat(insertRoomRsp2.getOwnerId()).isEqualTo(insertMemberRsp.getUserId());
        assertThat(insertRoomRsp2.getMembers().size()).isEqualTo(2);

        var members3 = Stream.of(insertWriterRsp.getUserId()).collect(toList());
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
                .shareIds(Stream.of(insertRoomRsp2.getRoomId() + 500, insertRoomRsp3.getRoomId()).collect(toList()))
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
                .member(Stream.of(insertMemberRsp.getUserId()).collect(toList()))
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
                .member(Stream.of(insertMemberRsp.getUserId()).collect(toList()))
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
        var findMemoriesRsp = memoryService.findMemories(insertMemoryRsp.getWriterId(), null);
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @DisplayName("일정 목록 조회 | 성공")
    @Transactional
    void findMemoriesSuccess() {
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
        var members = Stream.of(insertMemberRsp.getUserId()).collect(toList());
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
