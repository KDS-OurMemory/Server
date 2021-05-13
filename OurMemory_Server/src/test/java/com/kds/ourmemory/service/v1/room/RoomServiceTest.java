package com.kds.ourmemory.service.v1.room;

import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
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
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @Transactional
    void Room_Create_Read_Delete() {
        /* 0-1. Create owner, member */
        User Creator = userRepo.save(User.builder()
                .snsId("Creator_snsId")
                .snsType(1)
                .pushToken("Creator Token")
                .name("Creator")
                .birthday("0724")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("Android")
                .build());

        User member1 = userRepo.save(User.builder()
                .snsId("Member1_snsId")
                .snsType(2)
                .pushToken("member1 Token")
                .name("Member1")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("iOS")
                .build());

        User member2 = userRepo.save(User.builder()
                .snsId("Member2_snsId")
                .snsType(2)
                .pushToken("Member2 Token")
                .name("Member2")
                .birthday("0807")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("iOS")
                .build());

        List<Long> member = new ArrayList<>();
        member.add(member1.getId());
        member.add(member2.getId());

        /* 0-2. Create request */
        InsertRoomDto.Request insertRoomRequest = new InsertRoomDto.Request("TestRoom", Creator.getId(), false, member);

        /* 1. Make room */
        InsertRoomDto.Response insertRoomResponse = roomService.insert(insertRoomRequest);
        assertThat(insertRoomResponse).isNotNull();
        assertThat(isNow(insertRoomResponse.getCreateDate())).isTrue();

        log.info("CreateDate: {} roomId: {}", insertRoomResponse.getCreateDate(), insertRoomResponse.getRoomId());

        /* 2. Find room list */
        List<Room> responseList = userRepo.findById(insertRoomRequest.getOwner())
                .map(user -> roomService.findRooms(user.getId()))
                .orElseThrow(() -> new RoomInternalServerException("Not Found Room."));

        assertThat(responseList).isNotNull();

        log.info("[Room_목록_Read]");
        responseList.forEach(room -> log.info(room.toString()));
        log.info("====================================================================================");

        /* 3. Delete room */
        DeleteRoomDto.Response deleteRoomResponse = roomService.delete(insertRoomResponse.getRoomId());

        assertThat(deleteRoomResponse).isNotNull();
        assertThat(isNow(deleteRoomResponse.getDeleteDate())).isTrue();

        log.info("deleteDate: {}", deleteRoomResponse.getDeleteDate());
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
