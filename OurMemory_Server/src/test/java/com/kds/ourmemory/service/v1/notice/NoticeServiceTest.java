package com.kds.ourmemory.service.v1.notice;

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
public class NoticeServiceTest {
    private final NoticeService noticeService;

    /**
     * Assert time format -> delete sec
     *
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;

    @Autowired
    private NoticeServiceTest(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @Transactional
    void Notice_Create_Read() {

    }
}
