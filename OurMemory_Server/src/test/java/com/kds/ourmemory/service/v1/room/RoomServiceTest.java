package com.kds.ourmemory.service.v1.room;

import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.FindRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.UpdateRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomServiceTest {
    private final RoomService roomService;

    private final UserRepository userRepo; // Add to work with user data

    /**
     * Assert time format -> delete sec
     * 
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;

    @Autowired
    private RoomServiceTest(RoomService roomService, UserRepository userRepo) {
        this.roomService = roomService;
        this.userRepo = userRepo;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @Transactional
    void Create_Read_Update_Delete() {
        /* 0-1. Create owner, member */
        User owner = userRepo.save(
                User.builder()
                .snsId("owner_snsId")
                .snsType(1)
                .pushToken("owner Token")
                .name("owner")
                .birthday("0724")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.ANDROID)
                .role(UserRole.USER)
                .build()
        );

        User member1 = userRepo.save(
                User.builder()
                .snsId("member1_snsId")
                .snsType(2)
                .pushToken("member1 Token")
                .name("member1")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.IOS)
                .role(UserRole.USER)
                .build()
        );

        User member2 = userRepo.save(
                User.builder()
                .snsId("member2_snsId")
                .snsType(2)
                .pushToken("member2 Token")
                .name("member2")
                .birthday("0807")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.IOS)
                .role(UserRole.USER)
                .build()
        );

        List<Long> member = new ArrayList<>();
        member.add(member1.getId());
        member.add(member2.getId());

        /* 0-2. Create request */
        InsertRoomDto.Request insertReq = new InsertRoomDto.Request("TestRoom", owner.getId(), false, member);
        UpdateRoomDto.Request updateReq = new UpdateRoomDto.Request("update room name", true);

        /* 1. Insert */
        InsertRoomDto.Response insertRsp = roomService.insert(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getOwnerId()).isEqualTo(owner.getId());
        assertThat(insertRsp.getMembers()).isNotNull();
        assertThat(insertRsp.getMembers().size()).isEqualTo(3);

        /* 2. Find rooms */
        List<Room> findRooms = roomService.findRooms(owner.getId(), null);
        assertThat(findRooms).isNotNull();

        findRooms = roomService.findRooms(null, "TestRoom");
        assertThat(findRooms).isNotNull();

        log.info("[Create_Read_Update_Delete] Find rooms");
        findRooms.forEach(room -> log.info(room.toString()));

        /* 3. Find before update */
        FindRoomDto.Response beforeFindRsp = roomService.find(insertRsp.getRoomId());
        assertThat(beforeFindRsp).isNotNull();
        assertThat(beforeFindRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(beforeFindRsp.isOpened()).isEqualTo(insertReq.isOpened());

        /* 4. Update */
        UpdateRoomDto.Response updateRsp = roomService.update(insertRsp.getRoomId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertThat(isNow(updateRsp.getUpdateDate())).isTrue();

        /* 5. Find after update */
        FindRoomDto.Response afterFindRsp = roomService.find(insertRsp.getRoomId());
        assertThat(afterFindRsp).isNotNull();
        assertThat(afterFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterFindRsp.isOpened()).isEqualTo(updateReq.getOpened());
        
        /* 6. Delete */
        DeleteRoomDto.Response deleteRsp = roomService.delete(insertRsp.getRoomId());
        assertThat(deleteRsp).isNotNull();
        assertThat(isNow(deleteRsp.getDeleteDate())).isTrue();

        /* 7. Find after delete */
        Long roomId = insertRsp.getRoomId();
        assertThat(roomId).isNotNull();
        assertThrows(
                RoomNotFoundException.class, () -> roomService.find(roomId)
        );

        log.info("deleteDate: {}", deleteRsp.getDeleteDate());
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
