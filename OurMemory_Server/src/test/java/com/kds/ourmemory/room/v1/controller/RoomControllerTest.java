package com.kds.ourmemory.room.v1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.room.v1.controller.dto.RoomRspDto;
import com.kds.ourmemory.room.v1.entity.Room;
import com.kds.ourmemory.room.v1.service.RoomService;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v1.entity.User;
import com.kds.ourmemory.user.v1.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

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

        // given
        when(roomService.findRooms(owner.getId(), null)).thenReturn(rooms);

        // when
        var responseDto = roomController.findRooms(owner.getId(), null);

        // then
        Assertions.assertThat(responseDto.getResultCode()).isEqualTo("S001");
        Assertions.assertThat(responseDto.getResponse().size()).isEqualTo(3);

        // check response data
        log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDto));
    }
}
