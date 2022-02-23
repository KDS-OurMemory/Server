package com.kds.ourmemory.v1.controller.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.v1.controller.room.dto.RoomRspDto;
import com.kds.ourmemory.v1.entity.room.Room;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.entity.user.UserRole;
import com.kds.ourmemory.v1.service.room.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @Test
    @DisplayName("방 목록 조회 요청-응답 | 성공")
    void findRoomsSuccess() throws JsonProcessingException {
        // given
        var owner = User.builder()
                .id(1L)
                .snsType(1)
                .snsId("snsId")
                .pushToken("pushToken")
                .push(false)
                .name("name")
                .birthday("0101")
                .solar(true)
                .birthdayOpen(true)
                .role(UserRole.ADMIN)
                .deviceOs(DeviceOs.IOS)
                .build();
        owner.updatePrivateRoomId(1L);

        var room1 = Room.builder()
                .id(1L)
                .owner(owner)
                .name("room1 name")
                .opened(true)
                .build();

        var room2 = Room.builder()
                .id(2L)
                .owner(owner)
                .name("room2 name")
                .opened(false)
                .build();

        var room3 = Room.builder()
                .id(3L)
                .owner(owner)
                .name("room3 name")
                .opened(true)
                .build();

        var rooms = Stream.of(room1, room2, room3).map(RoomRspDto::new).collect(Collectors.toList());

        // when
        when(roomService.findRooms(owner.getId(), null)).thenReturn(rooms);

        // then
        var responseDto = roomController.findRooms(owner.getId(), null);
        assertThat(responseDto.getResultCode()).isEqualTo("S001");
        assertThat(responseDto.getResponse().size()).isEqualTo(3);

        // check response data
        log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDto));
    }
}
