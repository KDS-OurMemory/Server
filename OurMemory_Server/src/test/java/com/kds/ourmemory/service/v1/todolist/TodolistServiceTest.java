package com.kds.ourmemory.service.v1.todolist;

import com.kds.ourmemory.advice.v1.todolist.exception.TodolistNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodolistServiceTest {
    private final TodolistService todolistService;

    /**
     * Assert time format -> delete sec
     *
     * This is because time difference occurs after todolist creation due to relation table work.
     */
    private DateTimeFormatter alertTimeFormat;  // todoDate

    @Autowired
    private TodolistServiceTest(TodolistService todolistService) {
        this.todolistService = todolistService;
    }

    @BeforeAll
    void setUp() {
        alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Test
    @Order(1)
    @DisplayName("TODO 리스트 생성")
    @Transactional
    void insert() {
        // TODO: 로직 작성 후 테스트 코드 작성
    }

}
