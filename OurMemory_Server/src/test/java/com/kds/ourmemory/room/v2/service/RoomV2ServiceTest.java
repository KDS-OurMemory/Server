package com.kds.ourmemory.room.v2.service;

import com.kds.ourmemory.memory.v2.controller.dto.MemoryInsertReqDto;
import com.kds.ourmemory.memory.v2.service.MemoryV2Service;
import com.kds.ourmemory.room.v1.advice.exception.RoomNotFoundException;
import com.kds.ourmemory.room.v2.controller.dto.RoomInsertReqDto;
import com.kds.ourmemory.room.v2.controller.dto.RoomInsertRspDto;
import com.kds.ourmemory.room.v2.controller.dto.RoomUpdateReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpRspDto;
import com.kds.ourmemory.user.v2.enums.DeviceOs;
import com.kds.ourmemory.user.v2.service.UserV2Service;
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
public class RoomV2ServiceTest {

    private final RoomV2Service roomV2Service;

    private final UserV2Service userV2Service;

    private final MemoryV2Service memoryV2Service;

    /**
     * Assert time format -> delete sec
     * <p>
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter alertTimeFormat;  // startTime, endTime, firstAlarm, secondAlarm format

    // Base data for test RoomService
    private UserSignUpRspDto insertOwnerRsp;

    private UserSignUpRspDto insertMember1Rsp;

    private UserSignUpRspDto insertMember2Rsp;

    private List<Long> roomMembers;

    @Autowired
    private RoomV2ServiceTest(
            RoomV2Service roomV2Service,
            UserV2Service userV2Service,
            MemoryV2Service memoryV2Service
    ) {
        this.roomV2Service = roomV2Service;
        this.userV2Service = userV2Service;
        this.memoryV2Service = memoryV2Service;
    }

    @BeforeAll
    void setUp() {
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Order(1)
    @Test
    void _1_방생성_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomInsertReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomV2Service.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
    }

    @Order(2)
    @Test
    void _2_방단일조회_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomInsertReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomV2Service.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());

        /* 2. Find */
        var roomFindRspDto = roomV2Service.find(insertRoomRsp.getRoomId());
        assertThat(roomFindRspDto.getRoomId()).isEqualTo(insertRoomRsp.getRoomId());
        assertThat(roomFindRspDto.getOwnerId()).isEqualTo(insertRoomRsp.getOwnerId());
    }

    @Order(3)
    @Test
    void _3_방목록조회_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertOwnerRoomReq1 = RoomInsertReqDto.builder()
                .name("ownerRoom1")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var insertOwnerRoomReq2 = RoomInsertReqDto.builder()
                .name("ownerRoom2")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var insertParticipantRoomReq1 = RoomInsertReqDto.builder()
                .name("participantRoom1")
                .userId(insertMember1Rsp.getUserId())
                .opened(false)
                .member(List.of(insertOwnerRsp.getUserId()))
                .build();

        var insertParticipantRoomReq2 = RoomInsertReqDto.builder()
                .name("participantRoom2")
                .userId(insertMember2Rsp.getUserId())
                .opened(true)
                .member(List.of(insertOwnerRsp.getUserId()))
                .build();

        /* 1. Insert */
        var insertOwnerRoomRsp2 = roomV2Service.insert(insertOwnerRoomReq2);
        assertThat(insertOwnerRoomRsp2.getOwnerId()).isEqualTo(insertOwnerRoomReq2.getUserId());
        assertThat(insertOwnerRoomRsp2.getName()).isEqualTo(insertOwnerRoomReq2.getName());
        assertThat(insertOwnerRoomRsp2.isOpened()).isEqualTo(insertOwnerRoomReq2.isOpened());
        assertMembers(insertOwnerRoomRsp2, insertOwnerRoomReq2);

        var insertOwnerRoomRsp1 = roomV2Service.insert(insertOwnerRoomReq1);
        assertThat(insertOwnerRoomRsp1.getOwnerId()).isEqualTo(insertOwnerRoomReq1.getUserId());
        assertThat(insertOwnerRoomRsp1.getName()).isEqualTo(insertOwnerRoomReq1.getName());
        assertThat(insertOwnerRoomRsp1.isOpened()).isEqualTo(insertOwnerRoomReq1.isOpened());
        assertMembers(insertOwnerRoomRsp1, insertOwnerRoomReq1);

        var insertParticipantRoomRsp1 = roomV2Service.insert(insertParticipantRoomReq1);
        assertThat(insertParticipantRoomRsp1.getOwnerId()).isEqualTo(insertParticipantRoomReq1.getUserId());
        assertThat(insertParticipantRoomRsp1.getName()).isEqualTo(insertParticipantRoomReq1.getName());
        assertThat(insertParticipantRoomRsp1.isOpened()).isEqualTo(insertParticipantRoomReq1.isOpened());
        assertMembers(insertParticipantRoomRsp1, insertParticipantRoomReq1);

        var insertParticipantRoomRsp2 = roomV2Service.insert(insertParticipantRoomReq2);
        assertThat(insertParticipantRoomRsp2.getOwnerId()).isEqualTo(insertParticipantRoomReq2.getUserId());
        assertThat(insertParticipantRoomRsp2.getName()).isEqualTo(insertParticipantRoomReq2.getName());
        assertThat(insertParticipantRoomRsp2.isOpened()).isEqualTo(insertParticipantRoomReq2.isOpened());
        assertMembers(insertParticipantRoomRsp2, insertParticipantRoomReq2);

        /* 2. Find rooms and check private room not found */
        var findRoomsRsp = roomV2Service.findRooms(insertOwnerRsp.getUserId(), null);
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

    @Order(4)
    @Test
    void _4_방장양도_성공() {
        /* 0-1. Set base data */
        setBaseData();

        var insertExcludeMemberReq = UserSignUpReqDto.builder()
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
        var insertExcludeMemberRsp = userV2Service.signUp(insertExcludeMemberReq);
        assertThat(insertExcludeMemberRsp).isNotNull();
        assertThat(insertExcludeMemberRsp.getUserId()).isNotNull();

        /* 0-2. Create request */
        var insertRoomReq = RoomInsertReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomV2Service.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Patch owner */
        var recommendOwnerRsp = roomV2Service.recommendOwner(insertRoomRsp.getRoomId(), insertMember1Rsp.getUserId());
        assertThat(recommendOwnerRsp).isNotNull();
        assertThat(recommendOwnerRsp.getOwnerId()).isEqualTo(insertMember1Rsp.getUserId());
    }

    @Order(5)
    @Test
    void _5_방정보수정_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomInsertReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        var updateRoomReq = RoomUpdateReqDto.builder()
                .name("update room name")
                .opened(true)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomV2Service.insert(insertRoomReq);
        assertThat(insertRoomRsp).isNotNull();
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Update */
        var updateRsp = roomV2Service.update(insertRoomRsp.getRoomId(), updateRoomReq);
        assertThat(updateRsp).isNotNull();
        assertThat(updateRsp.getName()).isEqualTo(updateRoomReq.getName());
        assertThat(updateRsp.isOpened()).isEqualTo(updateRoomReq.isOpened());
    }

    @Order(6)
    @Test
    void _6_방삭제_공유방_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomInsertReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomV2Service.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getMembers()).isNotNull();
        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(3);

        /* 2. Insert Memories */
        var insertMemoryReqOwner = MemoryInsertReqDto.builder()
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

        var insertMemoryRspOwner = memoryV2Service.insert(insertMemoryReqOwner);
        assertThat(insertMemoryRspOwner.getWriterId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertMemoryRspOwner.getAddedRoomId()).isEqualTo(insertMemoryReqOwner.getRoomId());

        var insertMemoryReqMember1 = MemoryInsertReqDto.builder()
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

        var insertMemoryRspMember1 = memoryV2Service.insert(insertMemoryReqMember1);
        assertThat(insertMemoryRspMember1.getWriterId()).isEqualTo(insertMember1Rsp.getUserId());
        assertThat(insertMemoryRspMember1.getAddedRoomId()).isEqualTo(insertMemoryReqMember1.getRoomId());

        var insertMemoryReqMember2 = MemoryInsertReqDto.builder()
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

        var insertMemoryRspMember2 = memoryV2Service.insert(insertMemoryReqMember2);
        assertThat(insertMemoryRspMember2.getWriterId()).isEqualTo(insertMember2Rsp.getUserId());
        assertThat(insertMemoryRspMember2.getAddedRoomId()).isEqualTo(insertMemoryReqMember2.getRoomId());

        /* 3. Delete share room */
        var deleteRsp = roomV2Service.delete(insertRoomRsp.getRoomId(), insertOwnerRsp.getUserId());
        assertNull(deleteRsp);

        /* 4. Find room and memories after delete */
        var roomId = insertRoomRsp.getRoomId();
        assertThrows(
                RoomNotFoundException.class, () -> roomV2Service.find(roomId)
        );

        // 4-1. Owner
        var ownerMemoryId = insertMemoryRspOwner.getMemoryId();
        var ownerShareRoomId = insertMemoryRspOwner.getAddedRoomId();
        var ownerPrivateRoomId = insertOwnerRsp.getPrivateRoomId();
        // 1) Check delete memory from share room
        assertThrows(
                RoomNotFoundException.class, () -> memoryV2Service.find(ownerMemoryId, ownerShareRoomId)
        );

        // 2) Check not delete memory
        var afterOwnerFindMemoryRsp = memoryV2Service.find(ownerMemoryId, ownerPrivateRoomId);
        assertThat(afterOwnerFindMemoryRsp).isNotNull();

        // 3) Check memory exists private room
        var afterOwnerFindPrivateRoomRsp = roomV2Service.find(insertOwnerRsp.getPrivateRoomId());
        assertThat(afterOwnerFindPrivateRoomRsp.getMemories().size()).isOne();


        // 4-2. Member1
        var member1MemoryId = insertMemoryRspMember1.getMemoryId();
        var member1ShareRoomId = insertMemoryRspMember1.getAddedRoomId();
        var member1PrivateRoomId = insertMember1Rsp.getPrivateRoomId();
        // 1) Check delete memory from share room
        assertThrows(
                RoomNotFoundException.class, () -> memoryV2Service.find(member1MemoryId, member1ShareRoomId)
        );

        // 2) Check not delete memory
        var afterMember1FindMemoryRsp = memoryV2Service.find(member1MemoryId, member1PrivateRoomId);
        assertThat(afterMember1FindMemoryRsp).isNotNull();

        // 3) Check memory exists private room
        var afterMember1FindPrivateRoomRsp = roomV2Service.find(insertMember1Rsp.getPrivateRoomId());
        assertThat(afterMember1FindPrivateRoomRsp.getMemories().size()).isOne();


        // 4-3. Member2
        var member2MemoryId = insertMemoryRspMember2.getMemoryId();
        var memberShare2RoomId = insertMemoryRspMember2.getAddedRoomId();
        var member2PrivateRoomId = insertMember2Rsp.getPrivateRoomId();
        // 1) Check delete memory from share room
        assertThrows(
                RoomNotFoundException.class, () -> memoryV2Service.find(member2MemoryId, memberShare2RoomId)
        );

        // 2) Check not delete memory
        var afterMember2FindMemoryRsp = memoryV2Service.find(member2MemoryId, member2PrivateRoomId);
        assertThat(afterMember2FindMemoryRsp).isNotNull();

        // 3) Check memory exists private room
        var afterMember2FindPrivateRoomRsp = roomV2Service.find(insertMember2Rsp.getPrivateRoomId());
        assertThat(afterMember2FindPrivateRoomRsp.getMemories().size()).isOne();
    }

    @Order(7)
    @Test
    void _7_방나가기_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertRoomReq = RoomInsertReqDto.builder()
                .name("TestRoom")
                .userId(insertOwnerRsp.getUserId())
                .opened(false)
                .member(roomMembers)
                .build();

        /* 1. Insert */
        var insertRoomRsp = roomV2Service.insert(insertRoomReq);
        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertOwnerRsp.getUserId());
        assertThat(insertRoomRsp.getName()).isEqualTo(insertRoomReq.getName());
        assertThat(insertRoomRsp.isOpened()).isEqualTo(insertRoomReq.isOpened());
        assertMembers(insertRoomRsp, insertRoomReq);

        /* 2. Exit room */
        var exitRoomRsp = roomV2Service.exit(
                insertRoomRsp.getRoomId(), insertRoomRsp.getOwnerId(), insertMember1Rsp.getUserId()
        );
        assertNull(exitRoomRsp);

        /* 3. Check recommended owner */
        var findRoomRsp = roomV2Service.find(insertRoomRsp.getRoomId());
        assertThat(findRoomRsp.getOwnerId()).isEqualTo(insertMember1Rsp.getUserId());
        assertThat(findRoomRsp.getMembers().size()).isEqualTo(2);

        for (var member : findRoomRsp.getMembers()) {
            assertNotEquals(member.getFriendId(), insertRoomRsp.getOwnerId());
        }

        /* 4. Check delete relation from user */
        var findUserRsp = roomV2Service.findRooms(insertRoomRsp.getOwnerId(), null);
        assertThat(findUserRsp.size()).isZero();
    }

    // life cycle: @Before -> @Test => separate => Not maintained
    // Call function in @Test function => maintained
    void setBaseData() {
        /* 1. Create Owner, Member1, Member2 */
        var insertOwnerReq = UserSignUpReqDto.builder()
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
        insertOwnerRsp = userV2Service.signUp(insertOwnerReq);
        assertThat(insertOwnerRsp).isNotNull();
        assertThat(insertOwnerRsp.getUserId()).isNotNull();

        var insertMember1Req = UserSignUpReqDto.builder()
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
        insertMember1Rsp = userV2Service.signUp(insertMember1Req);
        assertThat(insertMember1Rsp).isNotNull();
        assertThat(insertMember1Rsp.getUserId()).isNotNull();

        var insertMember2Req = UserSignUpReqDto.builder()
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
        insertMember2Rsp = userV2Service.signUp(insertMember2Req);
        assertThat(insertMember2Rsp).isNotNull();
        assertThat(insertMember2Rsp.getUserId()).isNotNull();

        roomMembers = List.of(insertMember1Rsp.getUserId(), insertMember2Rsp.getUserId());
    }

    void assertMembers(RoomInsertRspDto roomRspDto, RoomInsertReqDto roomReqDto) {
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
