package com.kds.ourmemory.service.v1.notice;

import com.kds.ourmemory.controller.v1.notice.dto.InsertNoticeDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.entity.notice.NoticeType;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoticeServiceTest {
    private final NoticeService noticeService;

    private final UserService userService;  // The creation process from adding to the deletion of the user.

    // Base data for test NoticeService
    private InsertUserDto.Response insertUserRsp;


    @Autowired
    private NoticeServiceTest(NoticeService noticeService, UserService userService) {
        this.noticeService = noticeService;
        this.userService = userService;
    }

    @Test
    @Order(1)
    @DisplayName("알림 생성-조회-삭제")
    @Transactional
    void Notice_Create_Read_Delete() {
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
        var responseList = noticeService.findNotices(insertUserRsp.getUserId());
        assertThat(responseList).isNotNull();
        assertThat(responseList.isEmpty()).isFalse();
        assertThat(responseList.size()).isEqualTo(2);

        for (Notice notice : responseList) {
            assertThat(StringUtils.equals(notice.getValue(), "testValue1")
                    || StringUtils.equals(notice.getValue(), "testValue2")).isTrue();
        }

        /* 3. Delete notice */
        var deleteNoticeRsp = noticeService.deleteNotice(responseList.get(0).getId());
        assertThat(deleteNoticeRsp).isNotNull();

        /* 4. Find notices after delete */
        var afterResponseList = noticeService.findNotices(insertUserRsp.getUserId());
        assertThat(afterResponseList).isNotNull();
        assertThat(afterResponseList.isEmpty()).isFalse();
        assertThat(afterResponseList.size()).isOne();
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
}
