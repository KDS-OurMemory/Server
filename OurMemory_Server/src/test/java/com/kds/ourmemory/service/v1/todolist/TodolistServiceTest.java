package com.kds.ourmemory.service.v1.todolist;

import com.kds.ourmemory.controller.v1.todolist.dto.InsertTodolistDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodolistServiceTest {

    private final TodolistService todolistService;

    private final UserService userService;  // The creation process from adding to the deletion of the todolist.

    private DateTimeFormatter dayFormat;  // todoDate

    // Base data for test memoryService
    private InsertUserDto.Response insertWriterRsp;

    @Autowired
    private TodolistServiceTest(TodolistService todolistService, UserService userService) {
        this.todolistService = todolistService;
        this.userService = userService;
    }

    @BeforeAll
    void setUp() {
        dayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @DisplayName("TODO 리스트 생성")
    @Transactional
    void insert() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertTodolistReq = new InsertTodolistDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDateTime.parse("2021-10-30 00", dayFormat)
        );

        /* 1. Make todolist */
        var insertTodolistRsp = todolistService.insert(insertTodolistReq);
        assertThat(insertTodolistRsp).isNotNull();
        assertThat(insertTodolistRsp.getWriterId()).isEqualTo(insertTodolistReq.getWriter());
        assertThat(insertTodolistRsp.getContents()).isEqualTo(insertTodolistReq.getContents());
        assertThat(insertTodolistRsp.getTodoDate()).isEqualTo(insertTodolistReq.getTodoDate());
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create Writer */
        var insertWriterReq = new InsertUserDto.Request(
                1, "writer_snsId", "member Token",
                "member", "0519", true,
                false, DeviceOs.IOS
        );
        insertWriterRsp = userService.signUp(insertWriterReq);
        assertThat(insertWriterRsp).isNotNull();
        assertThat(insertWriterRsp.getUserId()).isNotNull();
        assertThat(insertWriterRsp.getPrivateRoomId()).isNotNull();
    }
}
