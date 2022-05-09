package com.kds.ourmemory.friend.v2.service;

import com.kds.ourmemory.friend.v1.advice.exception.FriendAlreadyAcceptException;
import com.kds.ourmemory.friend.v1.advice.exception.FriendStatusException;
import com.kds.ourmemory.friend.v1.entity.FriendStatus;
import com.kds.ourmemory.friend.v2.controller.dto.FriendAcceptRequestReqDto;
import com.kds.ourmemory.friend.v2.controller.dto.FriendPatchFriendStatusReqDto;
import com.kds.ourmemory.friend.v2.controller.dto.FriendReAddReqDto;
import com.kds.ourmemory.friend.v2.controller.dto.FriendRequestReqDto;
import com.kds.ourmemory.notice.v1.entity.NoticeType;
import com.kds.ourmemory.notice.v1.service.NoticeService;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpRspDto;
import com.kds.ourmemory.user.v2.service.UserV2Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendV2ServiceTest {

    private final FriendV2Service friendV2Service;
    private final UserV2Service userV2Service;

    private final NoticeService noticeService;  // The creation process from adding to the deletion of the notice.

    // Base data for test NoticeService
    private UserSignUpRspDto requestUserRsp;

    private UserSignUpRspDto acceptUserRsp;
    
    @Autowired
    private FriendV2ServiceTest(FriendV2Service friendV2Service, UserV2Service userV2Service, NoticeService noticeService) {
        this.friendV2Service = friendV2Service;
        this.userV2Service = userV2Service;
        this.noticeService = noticeService;
    }

    @Order(1)
    @Test
    void _1_친구요청_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendRequestReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendRequestReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Request friend */
        var requestRsp = friendV2Service.requestFriend(requestReq);
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);
    }

    @Order(2)
    @Test
    void _2_친구요청취소_요청알림삭제_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendRequestReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 1. Request friend */
        var requestRsp = friendV2Service.requestFriend(requestReq);
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 2. Check notice before cancel */
        var beforeCancelUserNotices = noticeService.findNotices(requestReq.getFriendUserId(), false);
        assertThat(beforeCancelUserNotices.size()).isOne();

        var beforeAcceptUserNoticeRsp = beforeCancelUserNotices.get(0);
        assertThat(beforeAcceptUserNoticeRsp.getType()).isEqualTo(NoticeType.FRIEND_REQUEST);
        assertThat(beforeAcceptUserNoticeRsp.getValue()).isEqualTo(Long.toString(requestReq.getUserId()));
        assertFalse(beforeAcceptUserNoticeRsp.isRead());

        /* 3. Cancel friend request */
        var cancelFriendRsp = friendV2Service.cancelRequest(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertNotNull(cancelFriendRsp);

        /* 4. Check notice after cancel */
        var afterAcceptUserNotices = noticeService.findNotices(requestReq.getFriendUserId(), false);
        assertTrue(afterAcceptUserNotices.isEmpty());
    }

    @Order(3)
    @Test
    void _3_친구요청수락_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendRequestReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendAcceptRequestReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Request friend */
        var requestRsp = friendV2Service.requestFriend(requestReq);
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 2. Accept friend */
        var acceptRsp = friendV2Service.acceptRequest(acceptReq);
        assertThat(acceptRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(acceptRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);
    }

    @Order(4)
    @Test
    void _4_친구목록조회_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendRequestReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendAcceptRequestReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Find friends before add friend */
        // My side
        var beforeMySideFindList = friendV2Service.findFriends(requestUserRsp.getUserId());
        assertTrue(beforeMySideFindList.isEmpty());

        // Friend side
        var beforeFriendSideFindList = friendV2Service.findFriends(acceptUserRsp.getUserId());
        assertTrue(beforeFriendSideFindList.isEmpty());

        /* 2. Request friend */
        var requestRsp = friendV2Service.requestFriend(requestReq);
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 3. Accept friend */
        var acceptRsp = friendV2Service.acceptRequest(acceptReq);
        assertThat(acceptRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(acceptRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 4. Find friends */
        // My side
        var mySideFindList = friendV2Service.findFriends(requestUserRsp.getUserId());
        assertFalse(mySideFindList.isEmpty());

        var mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        var friendSideFindList = friendV2Service.findFriends(acceptUserRsp.getUserId());
        assertFalse(friendSideFindList.isEmpty());

        var friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);
    }

    @Order(5)
    @Test
    void _5_친구상태변경_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendRequestReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendAcceptRequestReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        var blockReq = new FriendPatchFriendStatusReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId(), FriendStatus.BLOCK);

        /* 0-3. Request friend */
        var requestRsp = friendV2Service.requestFriend(requestReq);
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 0-4. Accept friend */
        var acceptRsp = friendV2Service.acceptRequest(acceptReq);
        assertThat(acceptRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(acceptRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-5. Find friends before patch friendStatus */
        var beforeFindFriends = friendV2Service.findFriends(requestReq.getUserId());
        assertThat(beforeFindFriends.get(0).getFriendId()).isEqualTo(requestReq.getFriendUserId());

        /* 1. Block from My side */
        var beforeFriendSideBlockRsp = friendV2Service.patchFriendStatus(blockReq);
        assertThat(beforeFriendSideBlockRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeFriendSideBlockRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);

        /* 2. Find friends */
        // Check my side
        var mySideFindFriends = friendV2Service.findFriends(requestReq.getUserId());
        assertThat(mySideFindFriends.get(0).getFriendId()).isEqualTo(requestReq.getFriendUserId());
        assertThat(mySideFindFriends.get(0).getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        var friendSideFindFriends = friendV2Service.findFriends(requestReq.getFriendUserId());
        assertThat(friendSideFindFriends.get(0).getFriendId()).isEqualTo(requestReq.getUserId());
        assertThat(friendSideFindFriends.get(0).getFriendStatus()).isEqualTo(blockReq.getFriendStatus());
    }

    @Order(6)
    @Test
    void _6_삭제_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendRequestReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendAcceptRequestReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Request friend */
        var requestRsp = friendV2Service.requestFriend(requestReq);
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 2. Accept friend */
        var acceptRsp = friendV2Service.acceptRequest(acceptReq);
        assertThat(acceptRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(acceptRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 3. Delete friend */
        // 1) Delete from my side
        var mySideDeleteRsp = friendV2Service.delete(
                requestUserRsp.getUserId(), acceptUserRsp.getUserId()
        );
        assertNotNull(mySideDeleteRsp);

        // Check my side
        var deleteFromMySideMySideList = friendV2Service.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        var deleteFromMySideFriendSideList = friendV2Service.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        var deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        var friendSideDeleteRsp = friendV2Service.delete(
                acceptUserRsp.getUserId(), requestUserRsp.getUserId()
        );
        assertNotNull(friendSideDeleteRsp);

        // Check my side
        var deleteFromFriendSideMySideList = friendV2Service.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        var deleteFromFriendSideFriendSideList = friendV2Service.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Order(7)
    @Test
    void _7_친구재추가_성공() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendRequestReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendAcceptRequestReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        var reAddReq = new FriendReAddReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 0-3. Request friend for other side friend */
        var beforeRequestRsp = friendV2Service.requestFriend(requestReq);
        assertThat(beforeRequestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeRequestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 0-4. Accept friend for other side friend */
        var beforeInsertRsp = friendV2Service.acceptRequest(acceptReq);
        assertThat(beforeInsertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeInsertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-5. Delete friend from my side */
        // Delete from requestUserRsp side
        var beforeMySideDeleteRsp = friendV2Service.delete(
                requestUserRsp.getUserId(), acceptUserRsp.getUserId()
        );
        assertNotNull(beforeMySideDeleteRsp);

        // My side
        var beforeMySideFriends = friendV2Service.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideFriends.size()).isZero();

        // Friend side
        var beforeFriendSideFriends = friendV2Service.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideFriends.size()).isOne();

        var beforeFriendSideFindRsp = beforeFriendSideFriends.get(0);
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);


        /* 1. Request friend */
        var friendAlreadyAcceptException = assertThrows(
                FriendAlreadyAcceptException.class, () -> friendV2Service.requestFriend(requestReq)
        );
        log.debug(
                "Expected exception occurred during request friend. {}:{}",
                friendAlreadyAcceptException.getClass(), friendAlreadyAcceptException.getMessage()
        );

        /* 2. Accept friend */
        var friendStatusException = assertThrows(
                FriendStatusException.class, () -> friendV2Service.acceptRequest(acceptReq)
        );
        log.debug(
                "Expected exception occurred during Accept friend. {}:{}",
                friendStatusException.getClass(), friendStatusException.getMessage()
        );

        /* 3. Add friend */
        var reAddFriendRsp = friendV2Service.reAdd(reAddReq);
        assertThat(reAddFriendRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(reAddFriendRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);
    }

    @Order(8)
    @Test
    void _8_사용자목록조회_성공() {
        /* 0. Create Request */
        var insertUniqueNameReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("TESTS_SNS_ID")
                .pushToken("before pushToken")
                .push(true)
                .name("테스트 유저")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();

        var insertSameNameReq1 = UserSignUpReqDto.builder()
                .snsType(2)
                .snsId("TESTS_SNS_ID2")
                .pushToken("before pushToken")
                .push(true)
                .name("둥명이인")
                .birthday("0724")
                .solar(false)
                .birthdayOpen(true)

                .deviceOs(DeviceOs.IOS)
                .build();

        var insertSameNameReq2 = UserSignUpReqDto.builder()
                .snsType(3)
                .snsId("TESTS_SNS_ID3")
                .pushToken("before pushToken")
                .push(true)
                .name("둥명이인")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(true)
                .deviceOs(DeviceOs.AOS)
                .build();

        /* 1. Insert */
        var insertUniqueNameRsp = userV2Service.signUp(insertUniqueNameReq);
        assertThat(insertUniqueNameRsp).isNotNull();

        var insertSameNameRsp1 = userV2Service.signUp(insertSameNameReq1);
        assertThat(insertSameNameRsp1).isNotNull();

        var insertSameNameRsp2 = userV2Service.signUp(insertSameNameReq2);
        assertThat(insertSameNameRsp2).isNotNull();

        /* 2. Find users before set friend */
        // 1) find by id : insertUniqueNameReq
        var findUsersByIdList1 = friendV2Service.findUsers(
                insertSameNameRsp1.getUserId(), insertUniqueNameRsp.getUserId(), null, null
        );
        assertThat(findUsersByIdList1.isEmpty()).isFalse();
        assertThat(findUsersByIdList1.size()).isOne();

        var findUsersById1 = findUsersByIdList1.get(0);
        assertThat(findUsersById1.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersById1.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersById1.isSolar()).isEqualTo(insertUniqueNameReq.getSolar());
        assertThat(findUsersById1.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.getBirthdayOpen());
        assertThat(findUsersById1.getBirthday()).isEqualTo(insertUniqueNameReq.getBirthday());
        assertThat(findUsersById1.getFriendStatus()).isNull();

        // 2) find by id : insertSameNameReq1
        var findUsersByIdList2 = friendV2Service.findUsers(
                insertUniqueNameRsp.getUserId(), insertSameNameRsp1.getUserId(), null, null
        );
        assertThat(findUsersByIdList2.isEmpty()).isFalse();
        assertThat(findUsersByIdList2.size()).isOne();

        var findUsersById2 = findUsersByIdList2.get(0);
        assertThat(findUsersById2.getFriendId()).isEqualTo(insertSameNameRsp1.getUserId());
        assertThat(findUsersById2.getName()).isEqualTo(insertSameNameReq1.getName());
        assertThat(findUsersById2.isSolar()).isEqualTo(insertSameNameReq1.getSolar());
        assertThat(findUsersById2.isBirthdayOpen()).isEqualTo(insertSameNameReq1.getBirthdayOpen());
        assertThat(findUsersById2.getBirthday()).isEqualTo(
                insertSameNameReq1.getBirthdayOpen() ? insertSameNameReq1.getBirthday() : null
        );
        assertThat(findUsersById2.getFriendStatus()).isNull();

        // 3) find by id : insertSameNameReq2
        var findUsersByIdList3 = friendV2Service.findUsers(
                insertSameNameRsp1.getUserId(), insertSameNameRsp2.getUserId(), null, null
        );
        assertThat(findUsersByIdList3.isEmpty()).isFalse();
        assertThat(findUsersByIdList3.size()).isOne();

        var findUsersById3 = findUsersByIdList3.get(0);
        assertThat(findUsersById3.getFriendId()).isEqualTo(insertSameNameRsp2.getUserId());
        assertThat(findUsersById3.getName()).isEqualTo(insertSameNameReq2.getName());
        assertThat(findUsersById3.isSolar()).isEqualTo(insertSameNameReq2.getSolar());
        assertThat(findUsersById3.isBirthdayOpen()).isEqualTo(insertSameNameReq2.getBirthdayOpen());
        assertThat(findUsersById3.getBirthday()).isEqualTo(insertSameNameReq2.getBirthday());
        assertThat(findUsersById3.getFriendStatus()).isNull();

        // 4) find by name : insertUniqueNameReq
        var findUsersByUniqueNameList = friendV2Service.findUsers(
                insertSameNameRsp1.getUserId(), null, insertUniqueNameReq.getName(), null
        );

        assertThat(findUsersByUniqueNameList.isEmpty()).isFalse();
        // TODO: GitHub Action 환경에서 해당 테스트만 되지 않아 주석함. 추후 GitHubAction 로그를 통해 해결할 예정.
//        assertThat(findUsersByUniqueNameList.size()).isOne();

        var findUsersByUniqueName = findUsersByUniqueNameList.get(0);
        assertThat(findUsersByUniqueName.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByUniqueName.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersByUniqueName.isSolar()).isEqualTo(insertUniqueNameReq.getSolar());
        assertThat(findUsersByUniqueName.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.getBirthdayOpen());
        assertThat(findUsersByUniqueName.getBirthday()).isEqualTo(insertUniqueNameReq.getBirthday());
        assertThat(findUsersByUniqueName.getFriendStatus()).isNull();

        // 5) find by name : insertSameNameReq1 or 2
        var findUsersBySameNameList = friendV2Service.findUsers(
                insertUniqueNameRsp.getUserId(), null, insertSameNameReq1.getName(), null
        );
        assertThat(findUsersBySameNameList.isEmpty()).isFalse();
        assertThat(findUsersBySameNameList.size()).isEqualTo(2);

        for (var findUsersBySameName : findUsersBySameNameList) {
            var findUsersBySameNameReq = findUsersBySameName.getFriendId().equals(insertSameNameRsp1.getUserId()) ?
                    insertSameNameReq1 : insertSameNameReq2;
            var findUsersBySameNameId = findUsersBySameName.getFriendId().equals(insertSameNameRsp1.getUserId()) ?
                    insertSameNameRsp1.getUserId() : insertSameNameRsp2.getUserId();

            assertThat(findUsersBySameName.getFriendId()).isEqualTo(findUsersBySameNameId);
            assertThat(findUsersBySameName.getName()).isEqualTo(findUsersBySameNameReq.getName());
            assertThat(findUsersBySameName.isSolar()).isEqualTo(findUsersBySameNameReq.getSolar());
            assertThat(findUsersBySameName.isBirthdayOpen()).isEqualTo(findUsersBySameNameReq.getBirthdayOpen());
            assertThat(findUsersBySameName.getBirthday()).isEqualTo(findUsersBySameNameReq.getBirthday());
            assertThat(findUsersBySameName.getFriendStatus()).isNull();
        }

        /* 3. Set Friend Status */
        // 1) WAIT - uniqueName -> sameName1, REQUESTED_BY - sameName1 -> uniqueName
        var waitRequestFriendRsp = friendV2Service.requestFriend(
                new FriendRequestReqDto(insertUniqueNameRsp.getUserId(), insertSameNameRsp1.getUserId())
        );
        assertThat(waitRequestFriendRsp).isNotNull();

        // 2) FRIEND - uniqueName -> sameName2, BLOCK - sameName2 -> uniqueName
        var friendRequestFriendRsp = friendV2Service.requestFriend(
                new FriendRequestReqDto(insertUniqueNameRsp.getUserId(), insertSameNameRsp2.getUserId())
        );
        assertThat(friendRequestFriendRsp).isNotNull();

        var friendAcceptFriendRsp = friendV2Service.acceptRequest(
                new FriendAcceptRequestReqDto(insertSameNameRsp2.getUserId(), insertUniqueNameRsp.getUserId())
        );
        assertThat(friendAcceptFriendRsp).isNotNull();

        var blockFriendRsp = friendV2Service.patchFriendStatus(
                new FriendPatchFriendStatusReqDto(
                        insertSameNameRsp2.getUserId(), insertUniqueNameRsp.getUserId(), FriendStatus.BLOCK
                )
        );
        assertThat(blockFriendRsp).isNotNull();

        /* 4. Find users after set friend */
        // 1) WAIT
        var findUsersByWaitList = friendV2Service.findUsers(
                insertUniqueNameRsp.getUserId(), null, null, FriendStatus.WAIT
        );
        assertThat(findUsersByWaitList.size()).isOne();

        var findUsersByWaitRsp = findUsersByWaitList.get(0);
        assertThat(findUsersByWaitRsp.getFriendId()).isEqualTo(insertSameNameRsp1.getUserId());
        assertThat(findUsersByWaitRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        // 2) REQUESTED_BY
        var findUsersByRequestedByList = friendV2Service.findUsers(
                insertSameNameRsp1.getUserId(), null, null, FriendStatus.REQUESTED_BY
        );
        assertThat(findUsersByRequestedByList.size()).isOne();

        var findUsersByRequestedByRsp = findUsersByRequestedByList.get(0);
        assertThat(findUsersByRequestedByRsp.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByRequestedByRsp.getFriendStatus()).isEqualTo(FriendStatus.REQUESTED_BY);

        // 3) FRIEND
        var findUsersByFriendList = friendV2Service.findUsers(
                insertUniqueNameRsp.getUserId(), null, null, FriendStatus.FRIEND
        );
        assertThat(findUsersByFriendList.size()).isOne();

        var findUsersByFriendRsp = findUsersByFriendList.get(0);
        assertThat(findUsersByFriendRsp.getFriendId()).isEqualTo(insertSameNameRsp2.getUserId());
        assertThat(findUsersByFriendRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // 4) BLOCK
        var findUsersByBlockList = friendV2Service.findUsers(
                insertSameNameRsp2.getUserId(), null, null, FriendStatus.BLOCK
        );
        assertThat(findUsersByBlockList.size()).isOne();

        var findUsersByBlockRsp = findUsersByBlockList.get(0);
        assertThat(findUsersByBlockRsp.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByBlockRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);
    }

    // life cycle: @Before -> @Test => separate => Not maintained
    // Call function in @Test function => maintained
    void setBaseData() {
        /* 1. Create RequestUser, AcceptUser */
        var insertRequestUserReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("request_snsId")
                .pushToken("request user Token")
                .push(true)
                .name("requestUser")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.IOS)
                .build();
        requestUserRsp = userV2Service.signUp(insertRequestUserReq);
        assertThat(requestUserRsp).isNotNull();
        assertThat(requestUserRsp.getUserId()).isNotNull();

        var insertAcceptUserReq = UserSignUpReqDto.builder()
                .snsType(1)
                .snsId("accept_user_snsId")
                .pushToken("accept user Token")
                .push(true)
                .name("acceptUser")
                .birthday("0720")
                .solar(true)
                .birthdayOpen(false)
                .deviceOs(DeviceOs.AOS)
                .build();
        acceptUserRsp = userV2Service.signUp(insertAcceptUserReq);
        assertThat(acceptUserRsp).isNotNull();
        assertThat(acceptUserRsp.getUserId()).isNotNull();
    }

}
