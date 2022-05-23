package com.kds.ourmemory.notice.v2.service;

import com.kds.ourmemory.notice.v2.enums.NoticeType;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeFindNoticesRspDto;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeInsertReqDto;
import com.kds.ourmemory.user.v2.enums.DeviceOs;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpRspDto;
import com.kds.ourmemory.user.v2.service.UserV2Service;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoticeV2ServiceTest {

    private final NoticeV2Service noticeV2Service;

    private final UserV2Service userV2Service;  // The creation process from adding to the deletion of the user.

    // Base data for test NoticeService
    private UserSignUpRspDto insertUserRsp;

    @Autowired
    private NoticeV2ServiceTest(NoticeV2Service noticeV2Service, UserV2Service userV2Service) {
        this.noticeV2Service = noticeV2Service;
        this.userV2Service = userV2Service;
    }

    @Order(1)
    @Test
    void _1_알림생성_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var insertReq1 = new NoticeInsertReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue1");
        var insertReq2 = new NoticeInsertReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeRsp1 = noticeV2Service.insert(insertReq1);
        assertThat(insertNoticeRsp1).isNotNull();
        assertThat(insertNoticeRsp1.getType()).isEqualTo(insertReq1.getNoticeType());
        assertThat(insertNoticeRsp1.getValue()).isEqualTo(insertReq1.getNoticeValue());
        assertTrue(isNow(insertNoticeRsp1.getRegDate()));

        var insertNoticeRsp2 = noticeV2Service.insert(insertReq2);
        assertThat(insertNoticeRsp2).isNotNull();
        assertThat(insertNoticeRsp2.getType()).isEqualTo(insertReq2.getNoticeType());
        assertThat(insertNoticeRsp2.getValue()).isEqualTo(insertReq2.getNoticeValue());
        assertTrue(isNow(insertNoticeRsp2.getRegDate()));
    }

    @Order(2)
    @Test
    void _2_알림목록조회_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new NoticeInsertReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue1");
        var request2 = new NoticeInsertReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue2");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeV2Service.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        var insertNoticeResponse2 = noticeV2Service.insert(request2);
        assertThat(insertNoticeResponse2).isNotNull();

        /* 2. Find notices */
        var findNoticesList = noticeV2Service.findNotices(insertUserRsp.getUserId(), false);
        assertThat(findNoticesList).isNotNull();
        assertThat(findNoticesList.size()).isEqualTo(2);

        for (NoticeFindNoticesRspDto findNoticesRsp : findNoticesList) {
            assertThat(StringUtils.equals(findNoticesRsp.getValue(), "testValue1")
                    || StringUtils.equals(findNoticesRsp.getValue(), "testValue2")).isTrue();
            assertFalse(findNoticesRsp.isRead());
        }

        /* 3. Check not read after find notices */
        var afterFindNoticesList = noticeV2Service.findNotices(insertUserRsp.getUserId(), true);
        assertThat(afterFindNoticesList).isNotNull();
        assertThat(afterFindNoticesList.size()).isEqualTo(2);

        for (NoticeFindNoticesRspDto findNoticesRsp : afterFindNoticesList) {
            assertFalse(findNoticesRsp.isRead());
        }
    }

    @Order(3)
    @Test
    void _3_알림삭제_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var request1 = new NoticeInsertReqDto(insertUserRsp.getUserId(), NoticeType.FRIEND_REQUEST, "testValue1");

        /* 1. Add notice */
        var insertNoticeResponse1 = noticeV2Service.insert(request1);
        assertThat(insertNoticeResponse1).isNotNull();

        /* 2. Delete notice */
        var deleteNoticeRsp = noticeV2Service.delete(insertNoticeResponse1.getNoticeId());
        assertNotNull(deleteNoticeRsp);
    }

    // life cycle: @Before -> @Test => separate => Not maintained
    // Call function in @Test function => maintained
    void setBaseData() {
        /* 1. Create User */
        var insertUserReq = UserSignUpReqDto.builder()
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
        insertUserRsp = userV2Service.signUp(insertUserReq);
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
