package com.kds.ourmemory.service.v1.notice;

import com.kds.ourmemory.controller.v1.notice.dto.InsertNoticeDto;
import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.entity.notice.NoticeType;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoticeServiceTest {
    private final NoticeService noticeService;

    private final UserRepository userRepo;  // Add to work with user data


    @Autowired
    private NoticeServiceTest(NoticeService noticeService, UserRepository userRepo) {
        this.noticeService = noticeService;
        this.userRepo = userRepo;
    }

    @Test
    @Order(1)
    @Transactional
    void Notice_Create_Read() {
        /* 0-1. Create user */
        User user = userRepo.save(User.builder()
                .snsId("user_snsId")
                .snsType(1)
                .pushToken("User Token")
                .name("User")
                .birthday("0724")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.ANDROID)
                .role(UserRole.USER)
                .build()
        );

        /* 0-2. Create request */
        InsertNoticeDto.Request request1 = new InsertNoticeDto.Request(user.getId(),
                NoticeType.FRIEND_REQUEST, "testValue1");
        InsertNoticeDto.Request request2 = new InsertNoticeDto.Request(user.getId(),
                NoticeType.FRIEND_REQUEST, "testValue2");


        /* 1. Add notice */
        InsertNoticeDto.Response insertNoticeResponse1 = noticeService.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        InsertNoticeDto.Response insertNoticeResponse2 = noticeService.insert(request2);
        assertThat(insertNoticeResponse2).isNotNull();

        /* 2. Find notices */
        List<Notice> responseList = noticeService.findNotices(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(responseList.isEmpty()).isFalse();
        assertThat(responseList.size()).isEqualTo(2);

        for (Notice notice : responseList) {
            assertThat(StringUtils.equals(notice.getValue(), "testValue1")
                    || StringUtils.equals(notice.getValue(), "testValue2")).isTrue();
        }
    }
}
