package com.kds.ourmemory.service.v1.todo;

import com.kds.ourmemory.controller.v1.todo.dto.InsertTodoDto;
import com.kds.ourmemory.controller.v1.todo.dto.UpdateTodoDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoServiceTest {

    private final TodoService todoService;

    private final UserService userService;  // The creation process from adding to the deletion of the todolist.

    // Base data for test memoryService
    private InsertUserDto.Response insertWriterRsp;

    @Autowired
    private TodoServiceTest(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    @Test
    @Order(1)
    @DisplayName("TODO 항목 생성")
    @Transactional
    void insert() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertTodoReq = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now()
        );

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(insertTodoReq);
        assertThat(insertTodoRsp).isNotNull();
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(insertTodoReq.getWriter());
        assertThat(insertTodoRsp.getContents()).isEqualTo(insertTodoReq.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(insertTodoReq.getTodoDate().atStartOfDay());
    }

    @Test
    @Order(2)
    @DisplayName("TODO 단일 조회")
    @Transactional
    void findTodo() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertTodoReq = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now()
        );

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(insertTodoReq);
        assertThat(insertTodoRsp).isNotNull();
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(insertTodoReq.getWriter());
        assertThat(insertTodoRsp.getContents()).isEqualTo(insertTodoReq.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(insertTodoReq.getTodoDate().atStartOfDay());

        /* 2. Find todoData */
        var findTodoRsp = todoService.find(insertTodoRsp.getTodoId());
        assertThat(findTodoRsp).isNotNull();
        assertThat(findTodoRsp.getTodoId()).isEqualTo(insertTodoRsp.getTodoId());
        assertThat(findTodoRsp.getWriterId()).isEqualTo(insertTodoRsp.getWriterId());
        assertThat(findTodoRsp.getContents()).isEqualTo(insertTodoRsp.getContents());
        assertThat(findTodoRsp.getTodoDate()).isEqualTo(insertTodoRsp.getTodoDate());
    }

    @Test
    @Order(3)
    @DisplayName("TODO 리스트 조회")
    @Transactional
    void findTodos() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertTodoReq1 = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now().minusDays(1)
        );

        var insertTodoReq2 = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now().plusDays(2)
        );

        var insertTodoReq3 = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now()
        );

        var insertTodoReq4 = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now().plusDays(1)
        );

        /* 1. Create todoData */
        var insertTodoRsp1 = todoService.insert(insertTodoReq1);
        assertThat(insertTodoRsp1.getWriterId()).isEqualTo(insertTodoReq1.getWriter());
        assertThat(insertTodoRsp1.getContents()).isEqualTo(insertTodoReq1.getContents());
        assertThat(insertTodoRsp1.getTodoDate()).isEqualTo(insertTodoReq1.getTodoDate().atStartOfDay());

        var insertTodoRsp2 = todoService.insert(insertTodoReq2);
        assertThat(insertTodoRsp2.getWriterId()).isEqualTo(insertTodoReq2.getWriter());
        assertThat(insertTodoRsp2.getContents()).isEqualTo(insertTodoReq2.getContents());
        assertThat(insertTodoRsp2.getTodoDate()).isEqualTo(insertTodoReq2.getTodoDate().atStartOfDay());

        var insertTodoRsp3 = todoService.insert(insertTodoReq3);
        assertThat(insertTodoRsp3.getWriterId()).isEqualTo(insertTodoReq3.getWriter());
        assertThat(insertTodoRsp3.getContents()).isEqualTo(insertTodoReq3.getContents());
        assertThat(insertTodoRsp3.getTodoDate()).isEqualTo(insertTodoReq3.getTodoDate().atStartOfDay());

        var insertTodoRsp4 = todoService.insert(insertTodoReq4);
        assertThat(insertTodoRsp4.getWriterId()).isEqualTo(insertTodoReq4.getWriter());
        assertThat(insertTodoRsp4.getContents()).isEqualTo(insertTodoReq4.getContents());
        assertThat(insertTodoRsp4.getTodoDate()).isEqualTo(insertTodoReq4.getTodoDate().atStartOfDay());

        /* 2. Find todos */
        var findTodosList = todoService.findTodos(insertWriterRsp.getUserId());
        // 1 => past todoDate -> expect not found.
        assertThat(findTodosList.size()).isEqualTo(2);

        // Expect order (1: not found) -> 3 -> 4 -> (2: not found)
        assertThat(findTodosList.get(0).getTodoId()).isEqualTo(insertTodoRsp3.getTodoId());
        assertThat(findTodosList.get(1).getTodoId()).isEqualTo(insertTodoRsp4.getTodoId());
    }

    @Test
    @Order(4)
    @DisplayName("TODO 수정")
    @Transactional
    void update() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertTodoReq = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now()
        );
        var updateTodoReq = new UpdateTodoDto.Request(
                "Update TODO contents!",
                LocalDate.now().plusDays(1)
        );

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(insertTodoReq);
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(insertTodoReq.getWriter());
        assertThat(insertTodoRsp.getContents()).isEqualTo(insertTodoReq.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(insertTodoReq.getTodoDate().atStartOfDay());

        /* 2. Find todos before update */
        var beforeFindTodoRsp = todoService.find(insertTodoRsp.getTodoId());
        assertThat(beforeFindTodoRsp.getTodoId()).isEqualTo(insertTodoRsp.getTodoId());
        assertThat(beforeFindTodoRsp.getContents()).isEqualTo(insertTodoRsp.getContents());
        assertThat(beforeFindTodoRsp.getTodoDate()).isEqualTo(insertTodoRsp.getTodoDate());

        /* 3. update todoData */
        var updateTodoRsp = todoService.update(insertTodoRsp.getTodoId(), updateTodoReq);
        assertThat(updateTodoRsp).isNotNull();

        /* 2. Find todos after delete */
        var afterFindTodoRsp = todoService.find(insertTodoRsp.getTodoId());
        assertThat(afterFindTodoRsp.getTodoId()).isEqualTo(insertTodoRsp.getTodoId());
        assertThat(afterFindTodoRsp.getContents()).isEqualTo(updateTodoReq.getContents());
        assertThat(afterFindTodoRsp.getTodoDate()).isEqualTo(updateTodoReq.getTodoDate().atStartOfDay());
    }

    @Test
    @Order(5)
    @DisplayName("TODO 삭제")
    @Transactional
    void delete() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertTodoReq = new InsertTodoDto.Request(
                insertWriterRsp.getUserId(),
                "Test TODO contents",
                LocalDate.now()
        );

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(insertTodoReq);
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(insertTodoReq.getWriter());
        assertThat(insertTodoRsp.getContents()).isEqualTo(insertTodoReq.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(insertTodoReq.getTodoDate().atStartOfDay());

        /* 2. Find todos before delete */
        var beforeFindTodosList = todoService.findTodos(insertWriterRsp.getUserId());
        assertThat(beforeFindTodosList.size()).isOne();

        var beforeFindTodosRsp = beforeFindTodosList.get(0);
        assertThat(beforeFindTodosRsp.getTodoId()).isEqualTo(insertTodoRsp.getTodoId());

        /* 3. Delete todoData */
        var deleteTodoRsp = todoService.delete(insertTodoRsp.getTodoId());
        assertThat(deleteTodoRsp).isNotNull();

        /* 2. Find todos after delete */
        var afterFindTodosList = todoService.findTodos(insertWriterRsp.getUserId());
        assertTrue(afterFindTodosList.isEmpty());
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
