package com.kds.ourmemory.service.v1.todo;

import com.kds.ourmemory.controller.v1.todo.dto.TodoReqDto;
import com.kds.ourmemory.controller.v1.user.dto.UserReqDto;
import com.kds.ourmemory.controller.v1.user.dto.UserRspDto;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoServiceTest {

    private final TodoService todoService;

    // The creation process from adding to the deletion of the todolist.
    private final UserService userService;

    // Base data for test memoryService
    private UserRspDto insertWriterRsp;

    @Autowired
    private TodoServiceTest(TodoService todoService, UserService userService) {
        this.todoService = todoService;
        this.userService = userService;
    }

    @Test
    @Order(1)
    @DisplayName("TODO 추가")
    @Transactional
    void insert() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(todoReqDto);
        assertThat(insertTodoRsp).isNotNull();
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(todoReqDto.getWriterId());
        assertThat(insertTodoRsp.getContents()).isEqualTo(todoReqDto.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(todoReqDto.getTodoDate());
    }

    @Test
    @Order(2)
    @DisplayName("TODO 단일 조회")
    @Transactional
    void findTodo() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(todoReqDto);
        assertThat(insertTodoRsp).isNotNull();
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(todoReqDto.getWriterId());
        assertThat(insertTodoRsp.getContents()).isEqualTo(todoReqDto.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(todoReqDto.getTodoDate());

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
    @DisplayName("TODO 목록 조회")
    @Transactional
    void findTodos() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto1 = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents1")
                .todoDate(LocalDate.now().minusDays(1))
                .build();

        var todoReqDto2 = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents2")
                .todoDate(LocalDate.now().plusDays(2))
                .build();

        var todoReqDto3 = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents3")
                .todoDate(LocalDate.now())
                .build();

        var todoReqDto4 = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents4")
                .todoDate(LocalDate.now().plusDays(1))
                .build();

        /* 1. Create todoData */
        var insertTodoRsp1 = todoService.insert(todoReqDto1);
        assertThat(insertTodoRsp1.getWriterId()).isEqualTo(todoReqDto1.getWriterId());
        assertThat(insertTodoRsp1.getContents()).isEqualTo(todoReqDto1.getContents());
        assertThat(insertTodoRsp1.getTodoDate()).isEqualTo(todoReqDto1.getTodoDate());

        var insertTodoRsp2 = todoService.insert(todoReqDto2);
        assertThat(insertTodoRsp2.getWriterId()).isEqualTo(todoReqDto2.getWriterId());
        assertThat(insertTodoRsp2.getContents()).isEqualTo(todoReqDto2.getContents());
        assertThat(insertTodoRsp2.getTodoDate()).isEqualTo(todoReqDto2.getTodoDate());

        var insertTodoRsp3 = todoService.insert(todoReqDto3);
        assertThat(insertTodoRsp3.getWriterId()).isEqualTo(todoReqDto3.getWriterId());
        assertThat(insertTodoRsp3.getContents()).isEqualTo(todoReqDto3.getContents());
        assertThat(insertTodoRsp3.getTodoDate()).isEqualTo(todoReqDto3.getTodoDate());

        var insertTodoRsp4 = todoService.insert(todoReqDto4);
        assertThat(insertTodoRsp4.getWriterId()).isEqualTo(todoReqDto4.getWriterId());
        assertThat(insertTodoRsp4.getContents()).isEqualTo(todoReqDto4.getContents());
        assertThat(insertTodoRsp4.getTodoDate()).isEqualTo(todoReqDto4.getTodoDate());

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
        var todoReqDto = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        var updateTodoReq = TodoReqDto.builder()
                .contents("Update TODO contents!")
                .todoDate(LocalDate.now().plusDays(1))
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(todoReqDto);
        assertThat(insertTodoRsp).isNotNull();
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(todoReqDto.getWriterId());
        assertThat(insertTodoRsp.getContents()).isEqualTo(todoReqDto.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(todoReqDto.getTodoDate());

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
        assertThat(afterFindTodoRsp.getTodoDate()).isEqualTo(updateTodoReq.getTodoDate());
    }

    @Test
    @Order(5)
    @DisplayName("TODO 삭제")
    @Transactional
    void delete() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var todoReqDto = TodoReqDto.builder()
                .writerId(insertWriterRsp.getUserId())
                .contents("Test TODO contents")
                .todoDate(LocalDate.now())
                .build();

        /* 1. Create todoData */
        var insertTodoRsp = todoService.insert(todoReqDto);
        assertThat(insertTodoRsp).isNotNull();
        assertThat(insertTodoRsp.getWriterId()).isEqualTo(todoReqDto.getWriterId());
        assertThat(insertTodoRsp.getContents()).isEqualTo(todoReqDto.getContents());
        assertThat(insertTodoRsp.getTodoDate()).isEqualTo(todoReqDto.getTodoDate());

        /* 2. Find todos before delete */
        var beforeFindTodosList = todoService.findTodos(insertWriterRsp.getUserId());
        assertThat(beforeFindTodosList.size()).isOne();

        var beforeFindTodosRsp = beforeFindTodosList.get(0);
        assertThat(beforeFindTodosRsp.getTodoId()).isEqualTo(insertTodoRsp.getTodoId());

        /* 3. Delete todoData */
        var deleteTodoRsp = todoService.delete(insertTodoRsp.getTodoId());
        assertNull(deleteTodoRsp);
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create Writer */
        var insertWriterReq = UserReqDto.builder()
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
        insertWriterRsp = userService.signUp(insertWriterReq);
        assertThat(insertWriterRsp).isNotNull();
        assertThat(insertWriterRsp.getUserId()).isNotNull();
    }
}
