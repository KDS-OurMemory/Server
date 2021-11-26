package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.friend.dto.FriendReqDto;
import com.kds.ourmemory.controller.v1.friend.dto.FriendRspDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.controller.v1.user.dto.UserDto;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.notice.NoticeType;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.service.v1.notice.NoticeService;
import com.kds.ourmemory.service.v1.user.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FriendServiceTest {
    private final FriendService friendService;

    private final UserService userService;  // The creation process from adding to the deletion of the user.

    private final NoticeService noticeService;  // The creation process from adding to the deletion of the notice.

    // Base data for test NoticeService
    private UserDto requestUserRsp;

    private UserDto acceptUserRsp;

    /**
     * Test case
     * ___________________________________________________________________
     * |My side friend|Friend side friend|   Block   | Add Friend My side|
     * |=================================================================|
     * |       X      |         X        |     X     |          O        |
     * |       X      |         O        |     X     |          O        |
     * |       X      |         O        |Friend side|          X        |
     * |       O      |         X        |     X     |          X        |
     * |       O      |         X        |  My side  |          X        |
     * |       O      |         O        |     X     |          X        |
     * -------------------------------------------------------------------
     */

    @Autowired
    private FriendServiceTest(FriendService friendService, UserService userService, NoticeService noticeService) {
        this.friendService = friendService;
        this.userService = userService;
        this.noticeService = noticeService;
    }

    @Test
    @Order(1)
    @DisplayName("내 편에서 친구[X] 상대편에서 친구[X]")
    @Transactional
    void bothNotFriend() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Request friend */
        var requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 2. Accept friend */
        var insertRsp = friendService.acceptFriend(acceptReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(insertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 2. Find friends */
        // My side
        List<FriendRspDto> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        var mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FriendRspDto> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        var friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 3. Delete friend */
        // 1) Delete from my side
        var mySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId())
        );
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        var deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        var friendSideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId())
        );
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(2)
    @DisplayName("내 편에서 친구[X] 상대편에서 친구[O] 차단[X]")
    @Transactional
    void onlyFriendSideAndNotBlock() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        var addReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 0-3. Request friend for other side friend */
        var beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(beforeRequestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeRequestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 0-4. Accept friend for other side friend */
        var beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(beforeInsertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeInsertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-5. Delete friend from my side */
        // Delete from requestUserRsp side
        var beforeMySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId())
        );
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // My side
        List<FriendRspDto> beforeMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideList.size()).isZero();

        // Friend side
        List<FriendRspDto> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideList.size()).isOne();

        var beforeFriendSideFindRsp = beforeFriendSideList.get(0);
        assertThat(beforeFriendSideFindRsp).isNotNull();
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);


        /* 1. Request friend */
        assertThrows(FriendAlreadyAcceptException.class, () ->
                friendService.requestFriend(requestReq)
        );

        /* 2. Accept friend */
        assertThrows(
                FriendStatusException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Add friend */
        var reAddRsp_MySideX_FriendSideO_Block = friendService.reAddFriend(addReq);
        assertThat(reAddRsp_MySideX_FriendSideO_Block).isNotNull();
        assertThat(reAddRsp_MySideX_FriendSideO_Block.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(reAddRsp_MySideX_FriendSideO_Block.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 4. Find friends */
        // My side
        List<FriendRspDto> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        var mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FriendRspDto> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        var friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 5. Delete friend */
        // 1) Delete from requestUserRsp side
        var mySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId())
        );
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        var deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        var friendSideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId())
        );
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(3)
    @DisplayName("내 편에서 친구[X] 상대편에서 친구[O] 차단[O]")
    @Transactional
    void onlyFriendSideAndBlock() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        var addReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var blockReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId(), FriendStatus.BLOCK);
        var cancelReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 0-3. Request friend for other side friend */
        var beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(beforeRequestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeRequestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 0-4. Accept friend for other side friend */
        var beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(beforeInsertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeInsertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-5. Delete friend from my side */
        // Delete from requestUserRsp side
        var beforeMySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId())
        );
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> beforeMySideFriends = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideFriends.size()).isZero();

        // Check friend side
        List<FriendRspDto> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideList.size()).isOne();

        var beforeFriendSideFindRsp = beforeFriendSideList.get(0);
        assertThat(beforeFriendSideFindRsp).isNotNull();
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-6. Block from friend side */
        var beforeFriendSideBlockRsp = friendService.patchFriendStatus(blockReq);
        assertThat(beforeFriendSideBlockRsp).isNotNull();
        assertThat(beforeFriendSideBlockRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeFriendSideBlockRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);


        /* 1. Request friend */
        var requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 2. Accept friend */
        assertThrows(
                FriendStatusException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Add friend */
        assertThrows(
                FriendBlockedException.class, () -> friendService.reAddFriend(addReq)
        );

        /* 4. Find friends */
        // My side
        List<FriendRspDto> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertThat(mySideFindList.size()).isOne();

        var mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        // Friend side
        List<FriendRspDto> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        var friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);

        /* 5. Delete friend */
        // 1) Delete from user side
        var userSideDeleteReqDto = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThrows(
                FriendInternalServerException.class, () -> friendService.deleteFriend(userSideDeleteReqDto)
        );

        // Cancel friend request
        var cancelRsp = friendService.cancelFriend(cancelReq);
        assertThat(cancelRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        var deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);

        // 2) Delete from friend side
        var friendSideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId())
        );
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(4)
    @DisplayName("내 편에서 친구[O] 상대편에서 친구[X] 차단[X]")
    @Transactional
    void onlyMySideAndNotBlock() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        var addReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 0-3. Request friend for my side friend */
        var beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(beforeRequestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeRequestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 0-4. Add friend for my side friend */
        var beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(beforeInsertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeInsertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-5. Delete requestUserRsp from friend side */
        // Delete requestUserRsp from friend
        var beforeMySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId())
        );
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> beforeMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideList.size()).isOne();

        var beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeMySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FriendRspDto> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideList.size()).isZero();


        /* 1. Request friend */
        assertThrows(
                IllegalArgumentException.class, () -> friendService.requestFriend(requestReq)
        );

        /* 2. Accept friend */
        assertThrows(
                FriendNotRequestedException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Add friend */
        assertThrows(
                FriendInternalServerException.class, () -> friendService.reAddFriend(addReq)
        );

        /* 4. Find friend */
        // My side
        List<FriendRspDto> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        var mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FriendRspDto> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertTrue(friendSideFindList.isEmpty());

        /* 5. Delete friend */
        // 1) Delete from requestUserRsp side
        var mySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId())
        );
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // 2) Delete from friend side
        var friendSideFriendReqDto = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendSideFriendReqDto)
        );

        // Check my side
        List<FriendRspDto> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(5)
    @DisplayName("내 편에서 친구[O] 상대편에서 친구[X] 차단[O]")
    @Transactional
    void onlyMySideAndBlock() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        var addReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var blockReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId(), FriendStatus.BLOCK);

        /* 0-3. Request friend for my side friend */
        var beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(beforeRequestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeRequestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 0-4. Add friend for my side friend */
        var beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(beforeInsertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeInsertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-5. Delete requestUserRsp from friend side */
        // Delete requestUserRsp from friend
        var beforeMySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId())
        );
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> beforeMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideList.size()).isOne();

        var beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeMySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FriendRspDto> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideList.size()).isZero();

        /* 0-6. Block from my side */
        var beforeMySideBlockRsp = friendService.patchFriendStatus(blockReq);
        assertThat(beforeMySideBlockRsp).isNotNull();
        assertThat(beforeMySideBlockRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeMySideBlockRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);


        /* 1. Request friend */
        assertThrows(
                IllegalArgumentException.class, () -> friendService.requestFriend(requestReq)
        );

        /* 2. Accept friend */
        assertThrows(
                FriendNotRequestedException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Add friend */
        assertThrows(
                FriendInternalServerException.class, () -> friendService.reAddFriend(addReq)
        );

        /* 4. Find friend */
        // My side
        List<FriendRspDto> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        var mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);

        // Friend side
        List<FriendRspDto> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isZero();

        /* 4. Delete friend */
        // 1) Delete from requestUserRsp side
        var mySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId())
        );
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // 2) Delete from friend side
        var friendSideFriendReqDto = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendSideFriendReqDto)
        );

        // Check my side
        List<FriendRspDto> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(6)
    @DisplayName("내 편에서 친구[O] 상대편에서 친구[O]")
    @Transactional
    void bothAlreadyFriend() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 0-3. Request friend for my side friend */
        var beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(beforeRequestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeRequestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 0-4. Add friend for both side friend */
        var beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(beforeInsertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeInsertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);


        /* 1. Request friend */
        assertThrows(
                IllegalArgumentException.class, () -> friendService.requestFriend(requestReq)
        );

        /* 2. Accept friend */
        assertThrows(
                FriendStatusException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Find friend */
        // My side
        List<FriendRspDto> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        var mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FriendRspDto> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isOne();

        var friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 4. Delete friend */
        // 1) Delete from requestUserRsp side
        var mySideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId())
        );
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        var deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        var friendSideDeleteRsp = friendService.deleteFriend(
                new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId())
        );
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FriendRspDto> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FriendRspDto> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(7)
    @DisplayName("친구 수락 후 관련 알림 읽음 확인")
    @Transactional
    void checkNoticeAfterAcceptFriend() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var acceptReq = new FriendReqDto(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Request friend */
        var requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
        assertThat(requestRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(requestRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        /* 2. Check notice before accept */
        var beforeAcceptUserNotices = noticeService.findNotices(requestReq.getFriendUserId(), false);
        assertThat(beforeAcceptUserNotices.size()).isOne();

        var beforeAcceptUserNoticeRsp = beforeAcceptUserNotices.get(0);
        assertThat(beforeAcceptUserNoticeRsp.getType()).isEqualTo(NoticeType.FRIEND_REQUEST);
        assertThat(beforeAcceptUserNoticeRsp.getValue()).isEqualTo(Long.toString(requestReq.getUserId()));
        assertFalse(beforeAcceptUserNoticeRsp.isRead());

        /* 3. Accept friend */
        var insertRsp = friendService.acceptFriend(acceptReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(insertRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 4. Check notice after accept */
        var afterAcceptUserNotices = noticeService.findNotices(requestReq.getFriendUserId(), false);
        assertThat(afterAcceptUserNotices.size()).isOne();

        var afterAcceptUserNoticeRsp = afterAcceptUserNotices.get(0);
        assertThat(afterAcceptUserNoticeRsp.getType()).isEqualTo(NoticeType.FRIEND_REQUEST);
        assertThat(afterAcceptUserNoticeRsp.getValue()).isEqualTo(Long.toString(requestReq.getUserId()));
        assertTrue(afterAcceptUserNoticeRsp.isRead());
    }

    @Test
    @Order(8)
    @DisplayName("친구 요청 취소 후 알림 삭제 확인")
    @Transactional
    void cancelFriendRequestAndCheckDeleteNotice() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        var requestReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        var cancelReq = new FriendReqDto(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 1. Request friend */
        var requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
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
        var cancelFriendRsp = friendService.cancelFriend(cancelReq);
        assertThat(cancelFriendRsp).isNotNull();

        /* 4. Check notice after cancel */
        var afterAcceptUserNotices = noticeService.findNotices(requestReq.getFriendUserId(), false);
        assertTrue(afterAcceptUserNotices.isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("사용자 목록 조회")
    @Transactional
    void findUsers() {
        /* 0. Create Request */
        var insertUniqueNameReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        var insertSameNameReq1 = new InsertUserDto.Request(
                2, "TESTS_SNS_ID2", "before pushToken",
                "동명이인", "0724", false,
                true, DeviceOs.IOS
        );

        var insertSameNameReq2 = new InsertUserDto.Request(
                3, "TESTS_SNS_ID3", "before pushToken",
                "동명이인", "0720", true,
                false, DeviceOs.ANDROID
        );

        /* 1. Insert */
        var insertUniqueNameRsp = userService.signUp(insertUniqueNameReq);
        assertThat(insertUniqueNameRsp).isNotNull();

        var insertSameNameRsp1 = userService.signUp(insertSameNameReq1);
        assertThat(insertSameNameRsp1).isNotNull();

        var insertSameNameRsp2 = userService.signUp(insertSameNameReq2);
        assertThat(insertSameNameRsp2).isNotNull();

        /* 2. Find users before set friend */
        // 1) find by id : insertUniqueNameReq
        var findUsersByIdList1 = friendService.findUsers(
                insertSameNameRsp1.getUserId(), insertUniqueNameRsp.getUserId(), null, null
        );
        assertThat(findUsersByIdList1).isNotNull();
        assertThat(findUsersByIdList1.isEmpty()).isFalse();
        assertThat(findUsersByIdList1.size()).isOne();

        var findUsersById1 = findUsersByIdList1.get(0);
        assertThat(findUsersById1).isNotNull();
        assertThat(findUsersById1.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersById1.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersById1.isSolar()).isEqualTo(insertUniqueNameReq.isSolar());
        assertThat(findUsersById1.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.isBirthdayOpen());
        assertThat(findUsersById1.getBirthday()).isEqualTo(insertUniqueNameReq.getBirthday());
        assertThat(findUsersById1.getFriendStatus()).isNull();

        // 2) find by id : insertSameNameReq1
        var findUsersByIdList2 = friendService.findUsers(
                insertUniqueNameRsp.getUserId(), insertSameNameRsp1.getUserId(), null, null
        );
        assertThat(findUsersByIdList2).isNotNull();
        assertThat(findUsersByIdList2.isEmpty()).isFalse();
        assertThat(findUsersByIdList2.size()).isOne();

        var findUsersById2 = findUsersByIdList2.get(0);
        assertThat(findUsersById2).isNotNull();
        assertThat(findUsersById2.getFriendId()).isEqualTo(insertSameNameRsp1.getUserId());
        assertThat(findUsersById2.getName()).isEqualTo(insertSameNameReq1.getName());
        assertThat(findUsersById2.isSolar()).isEqualTo(insertSameNameReq1.isSolar());
        assertThat(findUsersById2.isBirthdayOpen()).isEqualTo(insertSameNameReq1.isBirthdayOpen());
        assertThat(findUsersById2.getBirthday()).isEqualTo(
                insertSameNameReq1.isBirthdayOpen()? insertSameNameReq1.getBirthday() : null
        );
        assertThat(findUsersById2.getFriendStatus()).isNull();

        // 3) find by id : insertSameNameReq2
        var findUsersByIdList3 = friendService.findUsers(
                insertSameNameRsp1.getUserId(), insertSameNameRsp2.getUserId(), null, null
        );
        assertThat(findUsersByIdList3).isNotNull();
        assertThat(findUsersByIdList3.isEmpty()).isFalse();
        assertThat(findUsersByIdList3.size()).isOne();

        var findUsersById3 = findUsersByIdList3.get(0);
        assertThat(findUsersById3).isNotNull();
        assertThat(findUsersById3.getFriendId()).isEqualTo(insertSameNameRsp2.getUserId());
        assertThat(findUsersById3.getName()).isEqualTo(insertSameNameReq2.getName());
        assertThat(findUsersById3.isSolar()).isEqualTo(insertSameNameReq2.isSolar());
        assertThat(findUsersById3.isBirthdayOpen()).isEqualTo(insertSameNameReq2.isBirthdayOpen());
        assertThat(findUsersById3.getBirthday()).isEqualTo(insertSameNameReq2.getBirthday());
        assertThat(findUsersById3.getFriendStatus()).isNull();

        // 4) find by name : insertUniqueNameReq
        var findUsersByUniqueNameList = friendService.findUsers(
                insertSameNameRsp1.getUserId(), null, insertUniqueNameReq.getName(), null
        );
        assertThat(findUsersByUniqueNameList).isNotNull();
        assertThat(findUsersByUniqueNameList.isEmpty()).isFalse();
        assertThat(findUsersByUniqueNameList.size()).isOne();

        var findUsersByUniqueName = findUsersByUniqueNameList.get(0);
        assertThat(findUsersByUniqueName).isNotNull();
        assertThat(findUsersByUniqueName.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByUniqueName.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersByUniqueName.isSolar()).isEqualTo(insertUniqueNameReq.isSolar());
        assertThat(findUsersByUniqueName.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.isBirthdayOpen());
        assertThat(findUsersByUniqueName.getBirthday()).isEqualTo(insertUniqueNameReq.getBirthday());
        assertThat(findUsersByUniqueName.getFriendStatus()).isNull();

        // 5) find by name : insertSameNameReq1 or 2
        var findUsersBySameNameList = friendService.findUsers(
                insertUniqueNameRsp.getUserId(), null, insertSameNameReq1.getName(), null
        );
        assertThat(findUsersBySameNameList).isNotNull();
        assertThat(findUsersBySameNameList.isEmpty()).isFalse();
        assertThat(findUsersBySameNameList.size()).isEqualTo(2);

        for (var findUsersBySameName : findUsersBySameNameList) {
            var findUsersBySameNameReq = findUsersBySameName.getFriendId().equals(insertSameNameRsp1.getUserId()) ?
                    insertSameNameReq1 : insertSameNameReq2;
            var findUsersBySameNameId = findUsersBySameName.getFriendId().equals(insertSameNameRsp1.getUserId()) ?
                    insertSameNameRsp1.getUserId() : insertSameNameRsp2.getUserId();

            assertThat(findUsersBySameName).isNotNull();
            assertThat(findUsersBySameName.getFriendId()).isEqualTo(findUsersBySameNameId);
            assertThat(findUsersBySameName.getName()).isEqualTo(findUsersBySameNameReq.getName());
            assertThat(findUsersBySameName.isSolar()).isEqualTo(findUsersBySameNameReq.isSolar());
            assertThat(findUsersBySameName.isBirthdayOpen()).isEqualTo(findUsersBySameNameReq.isBirthdayOpen());
            assertThat(findUsersBySameName.getBirthday()).isEqualTo(findUsersBySameNameReq.getBirthday());
            assertThat(findUsersBySameName.getFriendStatus()).isNull();
        }

        /* 3. Set Friend Status */
        // 1) WAIT - uniqueName -> sameName1, REQUESTED_BY - sameName1 -> uniqueName
        var waitRequestFriendRsp = friendService.requestFriend(
                new FriendReqDto(insertUniqueNameRsp.getUserId(), insertSameNameRsp1.getUserId())
        );
        assertThat(waitRequestFriendRsp).isNotNull();

        // 2) FRIEND - uniqueName -> sameName2, BLOCK - sameName2 -> uniqueName
        var friendRequestFriendRsp = friendService.requestFriend(
                new FriendReqDto(insertUniqueNameRsp.getUserId(), insertSameNameRsp2.getUserId())
        );
        assertThat(friendRequestFriendRsp).isNotNull();

        var friendAcceptFriendRsp = friendService.acceptFriend(
                new FriendReqDto(insertSameNameRsp2.getUserId(), insertUniqueNameRsp.getUserId())
        );
        assertThat(friendAcceptFriendRsp).isNotNull();

        var blockFriendRsp = friendService.patchFriendStatus(
                new FriendReqDto(insertSameNameRsp2.getUserId(), insertUniqueNameRsp.getUserId(), FriendStatus.BLOCK)
        );
        assertThat(blockFriendRsp).isNotNull();

        /* 4. Find users after set friend */
        // 1) WAIT
        var findUsersByWaitList = friendService.findUsers(
                insertUniqueNameRsp.getUserId(), null, null, FriendStatus.WAIT
        );
        assertThat(findUsersByWaitList).isNotNull();
        assertThat(findUsersByWaitList.size()).isOne();

        var findUsersByWaitRsp = findUsersByWaitList.get(0);
        assertThat(findUsersByWaitRsp).isNotNull();
        assertThat(findUsersByWaitRsp.getFriendId()).isEqualTo(insertSameNameRsp1.getUserId());
        assertThat(findUsersByWaitRsp.getFriendStatus()).isEqualTo(FriendStatus.WAIT);

        // 2) REQUESTED_BY
        var findUsersByRequestedByList = friendService.findUsers(
                insertSameNameRsp1.getUserId(), null, null, FriendStatus.REQUESTED_BY
        );
        assertThat(findUsersByRequestedByList).isNotNull();
        assertThat(findUsersByRequestedByList.size()).isOne();

        var findUsersByRequestedByRsp = findUsersByRequestedByList.get(0);
        assertThat(findUsersByRequestedByRsp).isNotNull();
        assertThat(findUsersByRequestedByRsp.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByRequestedByRsp.getFriendStatus()).isEqualTo(FriendStatus.REQUESTED_BY);

        // 3) FRIEND
        var findUsersByFriendList = friendService.findUsers(
                insertUniqueNameRsp.getUserId(), null, null, FriendStatus.FRIEND
        );
        assertThat(findUsersByFriendList).isNotNull();
        assertThat(findUsersByFriendList.size()).isOne();

        var findUsersByFriendRsp = findUsersByFriendList.get(0);
        assertThat(findUsersByFriendRsp).isNotNull();
        assertThat(findUsersByFriendRsp.getFriendId()).isEqualTo(insertSameNameRsp2.getUserId());
        assertThat(findUsersByFriendRsp.getFriendStatus()).isEqualTo(FriendStatus.FRIEND);

        // 4) BLOCK
        var findUsersByBlockList = friendService.findUsers(
                insertSameNameRsp2.getUserId(), null, null, FriendStatus.BLOCK
                );
        assertThat(findUsersByBlockList).isNotNull();
        assertThat(findUsersByBlockList.size()).isOne();

        var findUsersByBlockRsp = findUsersByBlockList.get(0);
        assertThat(findUsersByBlockRsp).isNotNull();
        assertThat(findUsersByBlockRsp.getFriendId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByBlockRsp.getFriendStatus()).isEqualTo(FriendStatus.BLOCK);
    }


    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 1. Create RequestUser, AcceptUser */
        var insertRequestUserReq = new InsertUserDto.Request(
                1, "request_snsId", "request user Token",
                "requestUser", "0519", true,
                false, DeviceOs.IOS
        );
        requestUserRsp = userService.signUp(insertRequestUserReq);
        assertThat(requestUserRsp).isNotNull();
        assertThat(requestUserRsp.getUserId()).isNotNull();

        var insertAcceptUserReq = new InsertUserDto.Request(
                1, "accept_user_snsId", "accept user Token",
                "acceptUser", "0720", true,
                false, DeviceOs.ANDROID
        );
        acceptUserRsp = userService.signUp(insertAcceptUserReq);
        assertThat(acceptUserRsp).isNotNull();
        assertThat(acceptUserRsp.getUserId()).isNotNull();
    }
}
