package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoomServiceTest {
    @Autowired private RoomService roomService;
    
    @Autowired private UserRepository userRepo; // 사용자를 생성하고 삭제하기 위해 추가
    @Autowired private UserService userService;
    
    private InsertRoomRequestDto insertRoomRequestDto;
    private InsertRoomResponseDto insertRoomResponseDto;
    
    /* 테스트용 사용자 */
    private User 생성자;
    private User 참여자1;
    private User 참여자2;
    
    @BeforeAll
    void setUp() {
        생성자 = userRepo.save(
                User.builder()
                    .snsId("생성자_snsId")
                    .snsType(1)
                    .pushToken("생성자 토큰")
                    .name("생성자")
                    .birthday("0724")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .build());
       
        참여자1 = userRepo.save(
                User.builder()
                    .snsId("참여자1_snsId")
                    .snsType(2)
                    .pushToken("참여자1 토큰")
                    .name("참여자1")
                    .birthday("0519")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .build());
        
        참여자2 = userRepo.save(
                User.builder()
                    .snsId("참여자2_snsId")
                    .snsType(2)
                    .pushToken("참여자2 토큰")
                    .name("참여자2")
                    .birthday("0807")
                    .solar(true)
                    .birthdayOpen(true)
                    .regDate(currentDate())
                    .used(true)
                    .build());
        
        
        List<Long> member = new ArrayList<>();
        member.add(참여자1.getId());
        member.add(참여자2.getId());
        
        insertRoomRequestDto = new InsertRoomRequestDto("테스트방", 생성자.getId(), false, member);
    }
    
    @Test
    @Order(1)
    void 방_생성() {
        insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
        assertThat(insertRoomResponseDto).isNotNull();
        assertThat(insertRoomResponseDto.getCreateDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} roomId: {}", insertRoomResponseDto.getCreateDate(), insertRoomResponseDto.getRoomId());
    }
    
    @Test
    @Order(2)
    @Transactional
    void 방_목록_조회() throws UserNotFoundException {
        List<Room> responseList = Optional.ofNullable(userRepo.findById(insertRoomRequestDto.getOwner()).get())
            .map(user -> roomService.findRooms(user.getId()))
            .orElseThrow(() -> new RoomInternalServerException("Not Found Room."));
        
        assertThat(responseList).isNotNull();
        
        log.info("[방_목록_조회]");
        responseList.stream().forEach(room -> log.info("id: {}, name: {}", room.getId(), room.getName()));
        log.info("====================================================================================");
    }
    
    @Test
    @Order(3)
    void 방_삭제() throws RoomInternalServerException {
        DeleteRoomResponseDto deleteRoomResponseDto = roomService.delete(insertRoomResponseDto.getRoomId());
        
        assertThat(deleteRoomResponseDto).isNotNull();
        assertThat(deleteRoomResponseDto.getDeleteDate()).isEqualTo(currentDate());
        
        log.info("deleteDate: {}", deleteRoomResponseDto.getDeleteDate());
    }
    
    @AfterAll
    void 사용자_삭제() {
        DeleteUserResponseDto deleteUserDto = userService.delete(생성자.getId());
        assertThat(deleteUserDto).isNotNull();
        assertThat(deleteUserDto.getDeleteDate()).isEqualTo(currentDate());
        
        deleteUserDto = userService.delete(참여자1.getId());
        assertThat(deleteUserDto).isNotNull();
        assertThat(deleteUserDto.getDeleteDate()).isEqualTo(currentDate());
        
        deleteUserDto = userService.delete(참여자2.getId());
        assertThat(deleteUserDto).isNotNull();
        assertThat(deleteUserDto.getDeleteDate()).isEqualTo(currentDate());
    }
}
