package com.kds.ourmemory.v1.service.notice;

import com.kds.ourmemory.v1.advice.notice.exception.NoticeNotFoundException;
import com.kds.ourmemory.v1.advice.notice.exception.NoticeNotFoundUserException;
import com.kds.ourmemory.v1.controller.notice.dto.NoticeReqDto;
import com.kds.ourmemory.v1.controller.notice.dto.NoticeRspDto;
import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import com.kds.ourmemory.v1.controller.user.dto.UserRspDto;
import com.kds.ourmemory.v1.entity.notice.NoticeType;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import com.kds.ourmemory.v1.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoticeServiceTest {
    private final NoticeService noticeService;

    private final UserService userService;  // The creation process from adding to the deletion of the user.

    // Base data for test NoticeService
    private UserRspDto insertUserRsp;


    @Autowired
    private NoticeServiceTest(NoticeService noticeService, UserService userService) {
        this.noticeService = noticeService;
        this.userService = userService;
    }

    @Test
    @DisplayName("알림 생성 | 성공")
    @Transactional
    void insertSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertReq1 = new NoticeReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue1");
        var insertReq2 = new NoticeReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeRsp1 = noticeService.insert(insertReq1);
        assertThat(insertNoticeRsp1).isNotNull();
        assertThat(insertNoticeRsp1.getType()).isEqualTo(insertReq1.getNoticeType());
        assertThat(insertNoticeRsp1.getValue()).isEqualTo(insertReq1.getNoticeValue());
        assertTrue(isNow(insertNoticeRsp1.getRegDate()));

        var insertNoticeRsp2 = noticeService.insert(insertReq2);
        assertThat(insertNoticeRsp2).isNotNull();
        assertThat(insertNoticeRsp2.getType()).isEqualTo(insertReq2.getNoticeType());
        assertThat(insertNoticeRsp2.getValue()).isEqualTo(insertReq2.getNoticeValue());
        assertTrue(isNow(insertNoticeRsp2.getRegDate()));
    }

    @Test
    @DisplayName("알림 생성 | 실패 | 잘못된 사용자번호")
    @Transactional
    void insertFailToWrongUserId() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertReq1 = new NoticeReqDto(
                insertUserRsp.getUserId() + 5000, NoticeType.FRIEND_REQUEST, "testValue1"
        );

        /* 1. Add notice */
        assertThrows(
                NoticeNotFoundUserException.class, () -> noticeService.insert(insertReq1)
        );
    }

    @Test
    @DisplayName("알림 목록 조회 -> 읽음처리 X | 성공")
    @Transactional
    void findsSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new NoticeReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue1");
        var request2 = new NoticeReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeService.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        var insertNoticeResponse2 = noticeService.insert(request2);
        assertThat(insertNoticeResponse2).isNotNull();

        /* 2. Find notices */
        var findNoticesList = noticeService.findNotices(insertUserRsp.getUserId(), false);
        assertThat(findNoticesList).isNotNull();
        assertThat(findNoticesList.size()).isEqualTo(2);

        for (NoticeRspDto findNoticesRsp : findNoticesList) {
            assertThat(StringUtils.equals(findNoticesRsp.getValue(), "testValue1")
                    || StringUtils.equals(findNoticesRsp.getValue(), "testValue2")).isTrue();
            assertFalse(findNoticesRsp.isRead());
        }

        /* 3. Check not read after find notices */
        var afterFindNoticesList = noticeService.findNotices(insertUserRsp.getUserId(), true);
        assertThat(afterFindNoticesList).isNotNull();
        assertThat(afterFindNoticesList.size()).isEqualTo(2);

        for (NoticeRspDto findNoticesRsp : afterFindNoticesList) {
            assertFalse(findNoticesRsp.isRead());
        }
    }


    @Test
    @DisplayName("알림 목록 조회 -> 읽음처리 O | 성공")
    @Transactional
    void checkReadAfterFindNoticesSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new NoticeReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue1");
        var request2 = new NoticeReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeService.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        var insertNoticeResponse2 = noticeService.insert(request2);
        assertThat(insertNoticeResponse2).isNotNull();

        /* 2. Find notices */
        var findNoticesList = noticeService.findNotices(insertUserRsp.getUserId(), true);
        assertThat(findNoticesList).isNotNull();
        assertThat(findNoticesList.size()).isEqualTo(2);

        for (NoticeRspDto findNoticesRsp : findNoticesList) {
            assertFalse(findNoticesRsp.isRead());
        }

        /* 3. Check read after find notices */
        var afterFindNoticesList = noticeService.findNotices(insertUserRsp.getUserId(), false);
        assertThat(afterFindNoticesList).isNotNull();
        assertThat(afterFindNoticesList.size()).isEqualTo(2);

        for (NoticeRspDto findNoticesRsp : afterFindNoticesList) {
            assertTrue(findNoticesRsp.isRead());
        }
    }


    @Test
    @DisplayName("알림 삭제 | 성공")
    @Transactional
    void deleteSuccess() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new NoticeReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue1");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeService.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        /* 2. Delete notice */
        var deleteNoticeRsp = noticeService.delete(insertNoticeResponse1.getNoticeId());
        assertNull(deleteNoticeRsp);
    }

    @Test
    @DisplayName("알림 삭제 | 실패 | 잘못된 알림번호")
    @Transactional
    void deleteFailToWrongNoticeId() {
        /* 1. Delete notice */
        assertThrows(
                NoticeNotFoundException.class, () -> noticeService.delete(-5000L)
        );
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create User */
        var insertUserReq = UserReqDto.builder()
                .snsType(1)
                .snsId("user_snsId")
                .pushToken("user Token")
                .push(true)
                .name("user")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        insertUserRsp = userService.signUp(insertUserReq);
        assertThat(insertUserRsp).isNotNull();
        assertThat(insertUserRsp.getUserId()).isNotNull();
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
