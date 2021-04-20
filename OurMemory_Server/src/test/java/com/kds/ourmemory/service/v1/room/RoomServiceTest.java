package com.kds.ourmemory.service.v1.room;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomServiceTest {
    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepo; // Add to work with user data

    /**
     * Assert time format -> delete sec
     * 
     * This is because time difference occurs after room creation due to relation
     * table work.
     */
    private DateTimeFormatter format;

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @Transactional
    void 방_생성_조회_삭제() {
        /**
         * 0-1. Create owner, member
         */
        User 생성자 = userRepo.save(User.builder().snsId("생성자_snsId").snsType(1).pushToken("생성자 토큰").name("생성자")
                .birthday("0724").solar(true).birthdayOpen(true).used(true).deviceOs("Android").build());

        User 참여자1 = userRepo.save(User.builder().snsId("참여자1_snsId").snsType(2).pushToken("참여자1 토큰").name("참여자1")
                .birthday("0519").solar(true).birthdayOpen(true).used(true).deviceOs("iOS").build());

        User 참여자2 = userRepo.save(User.builder().snsId("참여자2_snsId").snsType(2).pushToken("참여자2 토큰").name("참여자2")
                .birthday("0807").solar(true).birthdayOpen(true).used(true).deviceOs("iOS").build());

        List<Long> member = new ArrayList<>();
        member.add(참여자1.getId());
        member.add(참여자2.getId());

        /**
         * 0-2. Create request
         */
        InsertRoomDto.Request insertRoomRequestDto = new InsertRoomDto.Request("테스트방", 생성자.getId(), false, member);

        /**
         * 1. Make room
         */
        InsertRoomDto.Response insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
        assertThat(insertRoomResponseDto).isNotNull();
        assertThat(LocalDateTime.parse(insertRoomResponseDto.getCreateDate(), format).format(format))
                .isEqualTo(LocalDateTime.now().format(format));

        log.info("CreateDate: {} roomId: {}", insertRoomResponseDto.getCreateDate(), insertRoomResponseDto.getRoomId());

        /**
         * 2. Find room list
         */
        List<Room> responseList = Optional.ofNullable(userRepo.findById(insertRoomRequestDto.getOwner()).get())
                .map(user -> roomService.findRooms(user.getId()))
                .orElseThrow(() -> new RoomInternalServerException("Not Found Room."));

        assertThat(responseList).isNotNull();

        log.info("[방_목록_조회]");
        responseList.stream().forEach(room -> log.info(room.toString()));
        log.info("====================================================================================");

        /**
         * 3. Delete room
         */
        DeleteRoomDto.Response deleteRoomResponseDto = roomService.delete(insertRoomResponseDto.getRoomId());

        assertThat(deleteRoomResponseDto).isNotNull();
        assertThat(LocalDateTime.parse(deleteRoomResponseDto.getDeleteDate(), format).format(format))
                .isEqualTo(LocalDateTime.now().format(format));

        log.info("deleteDate: {}", deleteRoomResponseDto.getDeleteDate());
    }
}
