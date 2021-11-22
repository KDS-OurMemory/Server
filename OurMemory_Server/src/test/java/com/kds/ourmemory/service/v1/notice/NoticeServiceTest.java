package com.kds.ourmemory.service.v1.notice;

import com.kds.ourmemory.controller.v1.notice.dto.InsertNoticeDto;
import com.kds.ourmemory.controller.v1.notice.dto.NoticeDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.controller.v1.user.dto.UserDto;
import com.kds.ourmemory.entity.notice.NoticeType;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoticeServiceTest {
    private final NoticeService noticeService;

    private final UserService userService;  // The creation process from adding to the deletion of the user.

    // Base data for test NoticeService
    private UserDto insertUserRsp;


    @Autowired
    private NoticeServiceTest(NoticeService noticeService, UserService userService) {
        this.noticeService = noticeService;
        this.userService = userService;
    }

    @Test
    @Order(1)
    @DisplayName("알림 생성")
    @Transactional
    void insert() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertReq1 = new InsertNoticeDto.Request(insertUserRsp.getUserId(),
                NoticeType.FRIEND_REQUEST, "testValue1");
        var insertReq2 = new InsertNoticeDto.Request(insertUserRsp.getUserId(),
                NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeRsp1 = noticeService.insert(insertReq1);
        assertThat(insertNoticeRsp1).isNotNull();
        assertThat(insertNoticeRsp1.getType()).isEqualTo(insertReq1.getType());
        assertThat(insertNoticeRsp1.getValue()).isEqualTo(insertReq1.getValue());
        assertTrue(isNow(insertNoticeRsp1.getRegDate()));

        var insertNoticeRsp2 = noticeService.insert(insertReq2);
        assertThat(insertNoticeRsp2).isNotNull();
        assertThat(insertNoticeRsp2.getType()).isEqualTo(insertReq2.getType());
        assertThat(insertNoticeRsp2.getValue()).isEqualTo(insertReq2.getValue());
        assertTrue(isNow(insertNoticeRsp2.getRegDate()));
    }

    @Test
    @Order(2)
    @DisplayName("알림 목록 조회")
    @Transactional
    void finds() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new InsertNoticeDto.Request(insertUserRsp.getUserId(),
                NoticeType.FRIEND_REQUEST, "testValue1");
        var request2 = new InsertNoticeDto.Request(insertUserRsp.getUserId(),
                NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeService.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        var insertNoticeResponse2 = noticeService.insert(request2);
        assertThat(insertNoticeResponse2).isNotNull();

        /* 2. Find notices */
        var findNoticesList = noticeService.findNotices(insertUserRsp.getUserId(), false);
        assertThat(findNoticesList).isNotNull();
        assertThat(findNoticesList.size()).isEqualTo(2);

        for (NoticeDto findNoticesRsp : findNoticesList) {
            assertThat(StringUtils.equals(findNoticesRsp.getValue(), "testValue1")
                    || StringUtils.equals(findNoticesRsp.getValue(), "testValue2")).isTrue();
            assertFalse(findNoticesRsp.isRead());
        }
    }

    @Test
    @Order(3)
    @DisplayName("알림 삭제")
    @Transactional
    void delete() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new InsertNoticeDto.Request(insertUserRsp.getUserId(),
                NoticeType.FRIEND_REQUEST, "testValue1");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeService.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        /* 2. Delete notice */
        var deleteNoticeRsp = noticeService.deleteNotice(insertNoticeResponse1.getNoticeId());
        assertThat(deleteNoticeRsp).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("조회된 알림 읽음 여부 확인")
    @Transactional
    void checkReadAfterFindNotices() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new InsertNoticeDto.Request(insertUserRsp.getUserId(),
                NoticeType.FRIEND_REQUEST, "testValue1");
        var request2 = new InsertNoticeDto.Request(insertUserRsp.getUserId(),
                NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeService.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        var insertNoticeResponse2 = noticeService.insert(request2);
        assertThat(insertNoticeResponse2).isNotNull();

        /* 2. Find notices */
        var findNoticesList = noticeService.findNotices(insertUserRsp.getUserId(), true);
        assertThat(findNoticesList).isNotNull();
        assertThat(findNoticesList.size()).isEqualTo(2);

        for (NoticeDto findNoticesRsp : findNoticesList) {
            assertFalse(findNoticesRsp.isRead());
        }

        /* 3. Check read after find notices */
        var afterFindNoticesList = noticeService.findNotices(insertUserRsp.getUserId(), true);
        assertThat(afterFindNoticesList).isNotNull();
        assertThat(afterFindNoticesList.size()).isEqualTo(2);

        for (NoticeDto findNoticesRsp : afterFindNoticesList) {
            assertTrue(findNoticesRsp.isRead());
        }
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create User */
        var insertUserReq = new InsertUserDto.Request(
                1, "user_snsId", "user Token",
                "user", "0519", true,
                false, DeviceOs.IOS
        );
        insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
        assertThat(insertUserRsp.getPrivateRoomId()).isNotNull();
    }

    boolean isNow(Object obj) {
        var rspFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var nowFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (obj instanceof String) {
            String targetTime = LocalDateTime.parse((String) obj, rspFormat).format(nowFormat);
            String nowTime = LocalDateTime.now().format(nowFormat);

            return StringUtils.equals(targetTime, nowTime);
        }

        return false;
    }
}
