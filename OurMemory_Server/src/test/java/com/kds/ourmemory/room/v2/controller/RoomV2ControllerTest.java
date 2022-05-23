package com.kds.ourmemory.room.v2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.room.v1.controller.dto.RoomRspDto;
import com.kds.ourmemory.room.v1.entity.Room;
import com.kds.ourmemory.room.v2.controller.dto.*;
import com.kds.ourmemory.room.v2.service.RoomV2Service;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v1.entity.User;
import com.kds.ourmemory.user.v1.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class RoomV2ControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private RoomV2Service roomV2Service;

    @InjectMocks
    private RoomV2Controller roomV2Controller;

    @Order(1)
    @Test
    void _1_방생성_성공() {
        var roomInsertReqDto = RoomInsertReqDto.builder()
                .name("room name")
                .userId(1L)
                .opened(false)
                .member(List.of(2L, 3L, 4L))
                .build();

        var owner = User.builder()
                .id(roomInsertReqDto.getUserId())
                .name("TEST NAME1")
                .snsType(1)
                .snsId("TEST SNS ID1")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var member1 = User.builder()
                .id(roomInsertReqDto.getMember().get(0))
                .name("TEST NAME2")
                .snsType(2)
                .snsId("TEST SNS ID2")
                .deviceOs(DeviceOs.IOS)
                .push(false)
                .build();

        var member2 = User.builder()
                .id(roomInsertReqDto.getMember().get(1))
                .name("SAME_NAME_2_3")
                .snsType(3)
                .snsId("TEST SNS ID3")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var member3 = User.builder()
                .id(roomInsertReqDto.getMember().get(2))
                .name("SAME_NAME_2_3")
                .snsType(1)
                .snsId("TEST SNS ID4")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var room = Room.builder()
                .id(1L)
                .owner(owner)
                .name(roomInsertReqDto.getName())
                .opened(roomInsertReqDto.isOpened())
                .build();
        room.addUser(member1);
        room.addUser(member2);
        room.addUser(member3);

        var roomRspDto = new RoomRspDto(room);
        var roomInsertRspDto = new RoomInsertRspDto(roomRspDto);

        // given
        given(roomV2Service.insert(any())).willReturn(roomInsertRspDto);

        // when
        var apiResult = roomV2Controller.insert(roomInsertReqDto);

        // then
        assertThat(apiResult.getResultCode()).isEqualTo("S001");
        assertThat(apiResult.getResponse()).isEqualTo(roomInsertRspDto);

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(2)
    @Test
    void _2_방단일조회_성공() {
        var roomInsertReqDto = RoomInsertReqDto.builder()
                .name("room name")
                .userId(1L)
                .opened(false)
                .member(List.of(2L, 3L, 4L))
                .build();

        var owner = User.builder()
                .id(roomInsertReqDto.getUserId())
                .name("TEST NAME1")
                .snsType(1)
                .snsId("TEST SNS ID1")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var member1 = User.builder()
                .id(roomInsertReqDto.getMember().get(0))
                .name("TEST NAME2")
                .snsType(2)
                .snsId("TEST SNS ID2")
                .deviceOs(DeviceOs.IOS)
                .push(false)
                .build();

        var member2 = User.builder()
                .id(roomInsertReqDto.getMember().get(1))
                .name("SAME_NAME_2_3")
                .snsType(3)
                .snsId("TEST SNS ID3")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var member3 = User.builder()
                .id(roomInsertReqDto.getMember().get(2))
                .name("SAME_NAME_2_3")
                .snsType(1)
                .snsId("TEST SNS ID4")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var room = Room.builder()
                .id(1L)
                .owner(owner)
                .name(roomInsertReqDto.getName())
                .opened(roomInsertReqDto.isOpened())
                .build();
        room.addUser(member1);
        room.addUser(member2);
        room.addUser(member3);

        var roomRspDto = new RoomRspDto(room);
        var roomFindRspDto = new RoomFindRspDto(roomRspDto);

        // given
        given(roomV2Service.find(anyLong())).willReturn(roomFindRspDto);

        // when
        var apiResult = roomV2Controller.find(room.getId());

        // then
        assertThat(apiResult.getResultCode()).isEqualTo("S001");
        assertThat(apiResult.getResponse()).isEqualTo(roomFindRspDto);

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(3)
    @Test
    void _3_방목록조회_성공() {
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

        var roomFindRspDtoList = Stream.of(room1, room2, room3)
                .map(RoomRspDto::new)
                .map(RoomFindRspDto::new)
                .collect(Collectors.toList());

        // given
        when(roomV2Service.findRooms(owner.getId(), null)).thenReturn(roomFindRspDtoList);

        // when
        var responseDto = roomV2Controller.findRooms(owner.getId(), null);

        // then
        Assertions.assertThat(responseDto.getResultCode()).isEqualTo("S001");
        Assertions.assertThat(responseDto.getResponse().size()).isEqualTo(3);

        // check response data
        printToPrettyJson(responseDto);
    }

    @Order(4)
    @Test
    void _4_방장양도_성공() {
        var roomInsertReqDto = RoomInsertReqDto.builder()
                .name("room name")
                .userId(1L)
                .opened(false)
                .member(List.of(2L, 3L, 4L))
                .build();

        var owner = User.builder()
                .id(roomInsertReqDto.getUserId())
                .name("TEST NAME1")
                .snsType(1)
                .snsId("TEST SNS ID1")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var member1 = User.builder()
                .id(roomInsertReqDto.getMember().get(0))
                .name("TEST NAME2")
                .snsType(2)
                .snsId("TEST SNS ID2")
                .deviceOs(DeviceOs.IOS)
                .push(false)
                .build();

        var member2 = User.builder()
                .id(roomInsertReqDto.getMember().get(1))
                .name("SAME_NAME_2_3")
                .snsType(3)
                .snsId("TEST SNS ID3")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var room = Room.builder()
                .id(1L)
                .owner(owner)
                .name(roomInsertReqDto.getName())
                .opened(roomInsertReqDto.isOpened())
                .build();
        room.addUser(member1);
        room.addUser(member2);

        var roomRspDto = new RoomRspDto(room);
        var roomRecommendOwnerRspDto = new RoomRecommendOwnerRspDto(roomRspDto);

        // given
        given(roomV2Service.recommendOwner(anyLong(), anyLong())).willReturn(roomRecommendOwnerRspDto);

        // when
        var apiResult = roomV2Controller.recommendOwner(room.getId(), owner.getId());

        // then
        assertThat(apiResult.getResultCode()).isEqualTo("S001");
        assertThat(apiResult.getResponse()).isEqualTo(roomRecommendOwnerRspDto);

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(5)
    @Test
    void _5_방정보수정_성공() {
        var roomInsertReqDto = RoomInsertReqDto.builder()
                .name("room name")
                .userId(1L)
                .opened(false)
                .member(List.of(2L, 3L, 4L))
                .build();

        var owner = User.builder()
                .id(roomInsertReqDto.getUserId())
                .name("TEST NAME1")
                .snsType(1)
                .snsId("TEST SNS ID1")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var member1 = User.builder()
                .id(roomInsertReqDto.getMember().get(0))
                .name("TEST NAME2")
                .snsType(2)
                .snsId("TEST SNS ID2")
                .deviceOs(DeviceOs.IOS)
                .push(false)
                .build();

        var room = Room.builder()
                .id(1L)
                .owner(owner)
                .name(roomInsertReqDto.getName())
                .opened(roomInsertReqDto.isOpened())
                .build();
        room.addUser(member1);

        var roomRspDto = new RoomRspDto(room);
        var roomUpdateRspDto = new RoomUpdateRspDto(roomRspDto);
        var roomUpdateReqDto = RoomUpdateReqDto.builder()
                .name("update room name")
                .opened(true)
                .build();

        // given
        given(roomV2Service.update(anyLong(), any())).willReturn(roomUpdateRspDto);

        // when
        var apiResult = roomV2Controller.update(room.getId(), roomUpdateReqDto);

        // then
        assertThat(apiResult.getResultCode()).isEqualTo("S001");
        assertThat(apiResult.getResponse()).isEqualTo(roomUpdateRspDto);

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(6)
    @Test
    void _6_방삭제_성공() {
        var roomInsertReqDto = RoomInsertReqDto.builder()
                .name("room name")
                .userId(1L)
                .opened(false)
                .member(List.of(2L, 3L, 4L))
                .build();

        var owner = User.builder()
                .id(roomInsertReqDto.getUserId())
                .name("TEST NAME1")
                .snsType(1)
                .snsId("TEST SNS ID1")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var member1 = User.builder()
                .id(roomInsertReqDto.getMember().get(0))
                .name("TEST NAME2")
                .snsType(2)
                .snsId("TEST SNS ID2")
                .deviceOs(DeviceOs.IOS)
                .push(false)
                .build();

        var room = Room.builder()
                .id(1L)
                .owner(owner)
                .name(roomInsertReqDto.getName())
                .opened(roomInsertReqDto.isOpened())
                .build();
        room.addUser(member1);

        // 삭제는 응답값을 주지 않기 때문에 given 세팅을 하지 않는다.

        // when
        var apiResult = roomV2Controller.delete(room.getId(), room.getOwner().getId());

        // then
        assertThat(apiResult.getResultCode()).isEqualTo("S001");
        assertThat(apiResult.getResponse()).isNull();

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(7)
    @Test
    void _7_방나가기_성공() {
        var roomInsertReqDto = RoomInsertReqDto.builder()
                .name("room name")
                .userId(1L)
                .opened(false)
                .member(List.of(2L, 3L, 4L))
                .build();

        var owner = User.builder()
                .id(roomInsertReqDto.getUserId())
                .name("TEST NAME1")
                .snsType(1)
                .snsId("TEST SNS ID1")
                .deviceOs(DeviceOs.AOS)
                .push(false)
                .build();

        var member1 = User.builder()
                .id(roomInsertReqDto.getMember().get(0))
                .name("TEST NAME2")
                .snsType(2)
                .snsId("TEST SNS ID2")
                .deviceOs(DeviceOs.IOS)
                .push(false)
                .build();

        var member2 = User.builder()
                .id(roomInsertReqDto.getMember().get(1))
                .name("SAME_NAME_2_3")
                .snsType(3)
                .snsId("TEST SNS ID3")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var room = Room.builder()
                .id(1L)
                .owner(owner)
                .name(roomInsertReqDto.getName())
                .opened(roomInsertReqDto.isOpened())
                .build();
        room.addUser(member1);
        room.addUser(member2);

        // given
//        given(roomV2Service.exit(anyLong(), anyLong(), anyLong())).willReturn(roomExitRspDto);

        // when
        var apiResult = roomV2Controller.exit(room.getId(), owner.getId(), room.getUsers().get(0).getId());

        // then
        assertThat(apiResult.getResultCode()).isEqualTo("S001");
        assertThat(apiResult.getResponse()).isNull();

        // check response data
        printToPrettyJson(apiResult);
    }

    private void printToPrettyJson(ApiResult apiResult) {
        try {
            log.debug("ApiResult: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiResult));
        } catch (JsonProcessingException e) {
            log.error("printError!", e);
        }
    }

}
