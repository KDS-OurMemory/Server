package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.friend.dto.*;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
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
    private InsertUserDto.Response requestUserRsp;
    
    private InsertUserDto.Response acceptUserRsp;

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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Request friend */
        RequestFriendDto.Response requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();

        /* 2. Accept friend */
        AcceptFriendDto.Response insertRsp = friendService.acceptFriend(acceptReq);
        assertThat(insertRsp).isNotNull();

        /* 2. Find friends */
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 3. Delete friend */
        // 1) Delete from my side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 0-3. Request friend for other side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Accept friend for other side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete friend from my side */
        // Delete from requestUserRsp side
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // My side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideList.size()).isZero();

        // Friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideList.size()).isOne();

        FindFriendsDto.Response beforeFriendSideFindRsp = beforeFriendSideList.get(0);
        assertThat(beforeFriendSideFindRsp).isNotNull();
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);


        /* 1. Request friend */
        assertThrows(FriendAlreadyAcceptException.class, () ->
                friendService.requestFriend(requestReq)
        );

        /* 2. Accept friend */
        assertThrows(
                FriendStatusException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Add friend */
        ReAddFriendDto.Response reAddRsp_MySideX_FriendSideO_Block = friendService.reAddFriend(addReq);
        assertThat(reAddRsp_MySideX_FriendSideO_Block).isNotNull();

        /* 4. Find friends */
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 5. Delete friend */
        // 1) Delete from requestUserRsp side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        PatchFriendStatusDto.Request blockReq = new PatchFriendStatusDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId(), FriendStatus.BLOCK);
        CancelFriendDto.Request cancelReq = new CancelFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 0-3. Request friend for other side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Accept friend for other side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete friend from my side */
        // Delete from requestUserRsp side
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideFriends = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideFriends.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideList.size()).isOne();

        FindFriendsDto.Response beforeFriendSideFindRsp = beforeFriendSideList.get(0);
        assertThat(beforeFriendSideFindRsp).isNotNull();
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(beforeFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 0-6. Block from friend side */
        PatchFriendStatusDto.Response beforeFriendSideBlockRsp = friendService.patchFriendStatus(blockReq);
        assertThat(beforeFriendSideBlockRsp).isNotNull();


        /* 1. Request friend */
        RequestFriendDto.Response requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();

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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertThat(mySideFindList.size()).isOne();

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.WAIT);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        /* 5. Delete friend */
        // 1) Delete from requestUserRsp side
        Long userId = requestUserRsp.getUserId();
        Long friendId = acceptUserRsp.getUserId();
        assertThrows(
                FriendInternalServerException.class, () -> friendService.deleteFriend(userId, friendId)
        );

        // Cancel friend request
        CancelFriendDto.Response cancelRsp = friendService.cancelFriend(cancelReq);
        assertThat(cancelRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList  = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Add friend for my side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete requestUserRsp from friend side */
        // Delete requestUserRsp from friend
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideList.size()).isOne();

        FindFriendsDto.Response beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeMySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertTrue(friendSideFindList.isEmpty());

        /* 5. Delete friend */
        // 1) Delete from requestUserRsp side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // 2) Delete from friend side
        Long userId = requestUserRsp.getUserId();
        Long friendId = acceptUserRsp.getUserId();
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendId, userId)
        );

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        PatchFriendStatusDto.Request blockReq = new PatchFriendStatusDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId(), FriendStatus.BLOCK);

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Add friend for my side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete requestUserRsp from friend side */
        // Delete requestUserRsp from friend
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(beforeMySideList.size()).isOne();

        FindFriendsDto.Response beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(beforeMySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(beforeFriendSideList.size()).isZero();

        /* 0-6. Block from my side */
        PatchFriendStatusDto.Response beforeMySideBlockRsp = friendService.patchFriendStatus(blockReq);
        assertThat(beforeMySideBlockRsp).isNotNull();


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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isZero();

        /* 4. Delete friend */
        // 1) Delete from requestUserRsp side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // 2) Delete from friend side
        Long userId = requestUserRsp.getUserId();
        Long friendId = acceptUserRsp.getUserId();
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendId, userId)
        );

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Add friend for both side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();


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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(acceptUserRsp.getUserId());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isOne();

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 4. Delete friend */
        // 1) Delete from requestUserRsp side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(requestUserRsp.getUserId());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(acceptUserRsp.getUserId(), requestUserRsp.getUserId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(requestUserRsp.getUserId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(acceptUserRsp.getUserId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(7)
    @DisplayName("친구 수락 후 관련 알림 삭제 확인")
    @Transactional
    void checkNoticeAfterAcceptFriend() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(requestUserRsp.getUserId(), acceptUserRsp.getUserId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(acceptUserRsp.getUserId(), requestUserRsp.getUserId());

        /* 1. Request friend */
        RequestFriendDto.Response requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
        
        /* 2. Check notice before accept */
        var beforeAcceptUserNotices = noticeService.findNotices(requestReq.getFriendUserId(), false);
        assertThat(beforeAcceptUserNotices.size()).isOne();

        var beforeAcceptUserNoticeRsp = beforeAcceptUserNotices.get(0);
        assertThat(beforeAcceptUserNoticeRsp.getType()).isEqualTo(NoticeType.FRIEND_REQUEST);
        assertThat(beforeAcceptUserNoticeRsp.getValue()).isEqualTo(Long.toString(requestReq.getUserId()));

        /* 3. Accept friend */
        AcceptFriendDto.Response insertRsp = friendService.acceptFriend(acceptReq);
        assertThat(insertRsp).isNotNull();

        /* 4. Check notice after accept */
        var afterAcceptUserNotices = noticeService.findNotices(requestReq.getFriendUserId(), false);
        assertTrue(afterAcceptUserNotices.isEmpty());
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
        assertThat(requestUserRsp.getPrivateRoomId()).isNotNull();

        var insertAcceptUserReq = new InsertUserDto.Request(
                1, "accept_user_snsId", "accept user Token",
                "acceptUser", "0720", true,
                false, DeviceOs.ANDROID
        );
        acceptUserRsp = userService.signUp(insertAcceptUserReq);
        assertThat(acceptUserRsp).isNotNull();
        assertThat(acceptUserRsp.getUserId()).isNotNull();
        assertThat(acceptUserRsp.getPrivateRoomId()).isNotNull();
    }
}
