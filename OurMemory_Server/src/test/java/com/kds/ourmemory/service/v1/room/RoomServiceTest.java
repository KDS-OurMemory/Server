package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

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
    @Autowired private RoomService roomService;
    
    @Autowired private UserRepository userRepo; // 사용자를 생성하고 삭제하기 위해 추가
    
    @Test
    @Order(1)
    @Transactional
    void 방_생성_조회_삭제() {
        /**
         * 0-1. 생성자, 참여자 생성
         */
        User 생성자 = userRepo.save(
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
       
        User 참여자1 = userRepo.save(
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
        
        User 참여자2 = userRepo.save(
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
        
        /**
         * 0-2. 요청 생성
         */
        InsertRoomDto.Request insertRoomRequestDto = new InsertRoomDto.Request("테스트방", 생성자.getId(), false, member);
        
        /**
         * 1. 방 생성
         */
        InsertRoomDto.Response insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
        assertThat(insertRoomResponseDto).isNotNull();
        assertThat(insertRoomResponseDto.getCreateDate()).isEqualTo(currentDate());
        
        log.info("CreateDate: {} roomId: {}", insertRoomResponseDto.getCreateDate(), insertRoomResponseDto.getRoomId());
        
        /**
         * 2. 방 목록 조회
         */
        List<Room> responseList = Optional.ofNullable(userRepo.findById(insertRoomRequestDto.getOwner()).get())
                .map(user -> roomService.findRooms(user.getId()))
                .orElseThrow(() -> new RoomInternalServerException("Not Found Room."));
            
        assertThat(responseList).isNotNull();
        
        log.info("[방_목록_조회]");
        responseList.stream().forEach(room -> log.info("id: {}, name: {}", room.getId(), room.getName()));
        log.info("====================================================================================");
            
        /**
         * 3. 방 삭제    
         */
        DeleteRoomDto.Response deleteRoomResponseDto = roomService.delete(insertRoomResponseDto.getRoomId());
        
        assertThat(deleteRoomResponseDto).isNotNull();
        assertThat(deleteRoomResponseDto.getDeleteDate()).isEqualTo(currentDate());
        
        log.info("deleteDate: {}", deleteRoomResponseDto.getDeleteDate());
    }
}
