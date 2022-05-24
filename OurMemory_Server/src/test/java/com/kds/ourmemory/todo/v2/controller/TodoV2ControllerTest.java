package com.kds.ourmemory.todo.v2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.todo.v1.controller.dto.TodoRspDto;
import com.kds.ourmemory.todo.v1.entity.Todo;
import com.kds.ourmemory.todo.v2.controller.dto.*;
import com.kds.ourmemory.todo.v2.service.TodoV2Service;
import com.kds.ourmemory.todo.v2.util.ObjectMapperFactory;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v1.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class TodoV2ControllerTest {

    private final ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

    @Mock
    private TodoV2Service todoV2Service;

    @InjectMocks
    private TodoV2Controller todoV2Controller;

    @Order(1)
    @Test
    void _1_할일생성_성공() {
        var todoInsertReqDto = TodoInsertReqDto.builder()
                .writerId(1L)
                .contents("Test Todo Contents")
                .todoDate(LocalDate.now().plusDays(1))
                .build();

        var writer = User.builder()
                .id(todoInsertReqDto.getWriterId())
                .name("Test Writer")
                .snsType(2)
                .snsId("Test Sns Id")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var todo = Todo.builder()
                .id(1L)
                .writer(writer)
                .contents(todoInsertReqDto.getContents())
                .todoDate(todoInsertReqDto.getTodoDate().atStartOfDay())
                .build();

        var todoRspDto = new TodoRspDto(todo);
        var todoInsertRspDto = new TodoInsertRspDto(todoRspDto);

        // given
        given(todoV2Service.insert(any())).willReturn(todoInsertRspDto);

        // when
        var apiResult = todoV2Controller.insert(todoInsertReqDto);

        // then
        assertAll(
                () -> assertEquals(apiResult.getResultCode(), "S001"),
                () -> assertEquals(apiResult.getResponse(), todoInsertRspDto)
        );

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(2)
    @Test
    void _2_할일단일조회_성공() {
        var writer = User.builder()
                .id(1L)
                .name("Test Writer")
                .snsType(2)
                .snsId("Test Sns Id")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var todo = Todo.builder()
                .id(1L)
                .writer(writer)
                .contents("Test Contents")
                .todoDate(LocalDateTime.now().plusDays(1))
                .build();

        var todoRspDto = new TodoRspDto(todo);
        var todoFindRspDto = new TodoFindRspDto(todoRspDto);

        // given
        given(todoV2Service.find(anyLong())).willReturn(todoFindRspDto);

        // when
        var apiResult = todoV2Controller.find(todo.getId());

        // then
        assertAll(
                () -> assertEquals(apiResult.getResultCode(), "S001"),
                () -> assertEquals(apiResult.getResponse(), todoFindRspDto)
        );

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(3)
    @Test
    void _3_할일목록조회_성공() {
        var writer = User.builder()
                .id(1L)
                .name("Test Writer")
                .snsType(2)
                .snsId("Test Sns Id")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var todo1 = Todo.builder()
                .id(1L)
                .writer(writer)
                .contents("Test Contents1")
                .todoDate(LocalDateTime.now().plusDays(1))
                .build();

        var todo2 = Todo.builder()
                .id(2L)
                .writer(writer)
                .contents("Test Contents2")
                .todoDate(LocalDateTime.now().plusDays(2))
                .build();

        var todo3 = Todo.builder()
                .id(3L)
                .writer(writer)
                .contents("Test Contents3")
                .todoDate(LocalDateTime.now().plusDays(1))
                .build();

        var todoFindRspDtoList = Stream.of(todo1, todo2, todo3)
                .map(TodoRspDto::new)
                .map(TodoFindRspDto::new)
                .toList();

        // given
        given(todoV2Service.findTodos(anyLong())).willReturn(todoFindRspDtoList);

        // when
        var apiResult = todoV2Controller.findTodos(writer.getId());

        // then
        assertAll(
                () -> assertEquals(apiResult.getResultCode(), "S001"),
                () -> assertEquals(apiResult.getResponse(), todoFindRspDtoList)
        );

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(4)
    @Test
    void _4_할일수정_성공() {
        var todoUpdateReqDto = TodoUpdateReqDto.builder()
                .contents("update contents!")
                .todoDate(LocalDate.now().plusDays(5))
                .build();

        var writer = User.builder()
                .id(1L)
                .name("Test Writer")
                .snsType(2)
                .snsId("Test Sns Id")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var todo = Todo.builder()
                .id(1L)
                .writer(writer)
                .contents(todoUpdateReqDto.getContents())
                .todoDate(todoUpdateReqDto.getTodoDate().atStartOfDay())
                .build();

        var todoRspDto = new TodoRspDto(todo);
        var todoUpdateRspDto = new TodoUpdateRspDto(todoRspDto);

        // given
        given(todoV2Service.update(anyLong(), any())).willReturn(todoUpdateRspDto);

        // when
        var apiResult = todoV2Controller.update(todo.getId(), todoUpdateReqDto);

        // then
        assertAll(
                () -> assertEquals(apiResult.getResultCode(), "S001"),
                () -> assertEquals(apiResult.getResponse(), todoUpdateRspDto)
        );

        // check response data
        printToPrettyJson(apiResult);
    }

    @Order(5)
    @Test
    void _5_할일삭제_성공() {
        var writer = User.builder()
                .id(1L)
                .name("Test Writer")
                .snsType(2)
                .snsId("Test Sns Id")
                .deviceOs(DeviceOs.IOS)
                .push(true)
                .build();

        var todo = Todo.builder()
                .id(1L)
                .writer(writer)
                .contents("Test Contents")
                .todoDate(LocalDateTime.now().plusDays(6))
                .build();

        // 삭제는 응답값을 주지 않기 때문에 given 세팅을 하지 않는다.

        // when
        var apiResult = todoV2Controller.delete(todo.getId());

        // then
        assertAll(
                () -> assertEquals(apiResult.getResultCode(), "S001"),
                () -> assertNull(apiResult.getResponse())
        );

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
