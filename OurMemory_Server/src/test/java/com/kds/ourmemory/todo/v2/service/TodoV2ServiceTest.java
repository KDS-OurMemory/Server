package com.kds.ourmemory.todo.v2.service;

import com.kds.ourmemory.todo.v1.advice.exception.TodoNotFoundException;
import com.kds.ourmemory.todo.v2.controller.dto.TodoInsertReqDto;
import com.kds.ourmemory.todo.v2.controller.dto.TodoUpdateReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpRspDto;
import com.kds.ourmemory.user.v2.enums.DeviceOs;
import com.kds.ourmemory.user.v2.service.UserV2Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoV2ServiceTest {

    private final TodoV2Service todoV2Service;

    private final UserV2Service userV2Service;

    private UserSignUpRspDto insertWriterRsp;

    @Autowired
    private TodoV2ServiceTest(TodoV2Service todoV2Service, UserV2Service userV2Service) {
        this.todoV2Service = todoV2Service;
        this.userV2Service = userV2Service;
    }

    @Order(1)
    @Test
    void _1_할일생성_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto = TodoInsertReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoV2Service.insert(todoReqDto);
        assertAll(
                () -> assertEquals(insertTodoRsp.getWriterId(), todoReqDto.getWriterId()),
                () -> assertEquals(insertTodoRsp.getContents(), todoReqDto.getContents()),
                () -> assertEquals(insertTodoRsp.getTodoDate(), todoReqDto.getTodoDate())
        );
    }

    @Order(2)
    @Test
    void _2_할일단일조회_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto = TodoInsertReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoV2Service.insert(todoReqDto);
        assertAll(
                () -> assertEquals(insertTodoRsp.getWriterId(), todoReqDto.getWriterId()),
                () -> assertEquals(insertTodoRsp.getContents(), todoReqDto.getContents()),
                () -> assertEquals(insertTodoRsp.getTodoDate(), todoReqDto.getTodoDate())
        );

        /* 2. Find todoData */
        var findTodoRsp = todoV2Service.find(insertTodoRsp.getTodoId());
        assertAll(
                () -> assertEquals(findTodoRsp.getTodoId(), insertTodoRsp.getTodoId()),
                () -> assertEquals(findTodoRsp.getWriterId(), insertTodoRsp.getWriterId()),
                () -> assertEquals(findTodoRsp.getContents(), insertTodoRsp.getContents()),
                () -> assertEquals(findTodoRsp.getTodoDate(), insertTodoRsp.getTodoDate())
        );
    }

    @Order(4)
    @Test
    void _4_할일수정_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto = TodoInsertReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        var updateTodoReq = TodoUpdateReqDto.builder()
                .contents("Update TODO contents!")
                .todoDate(LocalDate.now().plusDays(1))
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoV2Service.insert(todoReqDto);
        assertAll(
                () -> assertEquals(insertTodoRsp.getWriterId(), todoReqDto.getWriterId()),
                () -> assertEquals(insertTodoRsp.getContents(), todoReqDto.getContents()),
                () -> assertEquals(insertTodoRsp.getTodoDate(), todoReqDto.getTodoDate())
        );

        /* 2. Find todos before update */
        var beforeFindTodoRsp = todoV2Service.find(insertTodoRsp.getTodoId());
        assertAll(
                () -> assertEquals(beforeFindTodoRsp.getTodoId(), insertTodoRsp.getTodoId()),
                () -> assertEquals(beforeFindTodoRsp.getContents(), insertTodoRsp.getContents()),
                () -> assertEquals(beforeFindTodoRsp.getTodoDate(), insertTodoRsp.getTodoDate())
        );

        /* 3. update todoData */
        var updateTodoRsp = todoV2Service.update(insertTodoRsp.getTodoId(), updateTodoReq);
        assertThat(updateTodoRsp).isNotNull();

        /* 2. Find todoData after update */
        var afterFindTodoRsp = todoV2Service.find(insertTodoRsp.getTodoId());
        assertAll(
                () -> assertEquals(afterFindTodoRsp.getTodoId(), insertTodoRsp.getTodoId()),
                () -> assertEquals(afterFindTodoRsp.getContents(), updateTodoRsp.getContents()),
                () -> assertEquals(afterFindTodoRsp.getTodoDate(), updateTodoRsp.getTodoDate())
        );
    }

    @Order(5)
    @Test
    void _5_할일삭제_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto = TodoInsertReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoV2Service.insert(todoReqDto);
        assertAll(
                () -> assertEquals(insertTodoRsp.getWriterId(), todoReqDto.getWriterId()),
                () -> assertEquals(insertTodoRsp.getContents(), todoReqDto.getContents()),
                () -> assertEquals(insertTodoRsp.getTodoDate(), todoReqDto.getTodoDate())
        );

        /* 2. Find todoData before delete */
        var beforeFindTodoRsp = todoV2Service.find(insertTodoRsp.getTodoId());
        assertEquals(beforeFindTodoRsp.getTodoId(), insertTodoRsp.getTodoId());

        /* 3. Delete todoData */
        var deleteTodoRsp = todoV2Service.delete(insertTodoRsp.getTodoId());
        assertNull(deleteTodoRsp);

        /* 4. Find todoData after delete */
        assertThrows(
                TodoNotFoundException.class,
                () -> todoV2Service.find(insertTodoRsp.getTodoId())
        );

        var afterFindTodoRspList = todoV2Service.findTodos(insertTodoRsp.getWriterId());
        assertThat(afterFindTodoRspList.size()).isZero();
    }

    // life cycle: @Before -> @Test => separate => Not maintained
    // Call function in @Test function => maintained
    void setBaseData() {
        /* 1. Create Writer */
        var insertWriterReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("writer_snsId")
                .pushToken("writer Token")
                .push(true)
                .name("writer")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        insertWriterRsp = userV2Service.signUp(insertWriterReq);
        assertThat(insertWriterRsp).isNotNull();
        assertThat(insertWriterRsp.getUserId()).isNotNull();
    }

}
