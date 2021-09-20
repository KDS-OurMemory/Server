package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.friend.dto.*;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
import com.kds.ourmemory.repository.user.UserRepository;
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

    // Add to work with user data
    private final UserRepository userRepo;

    // Base data for test RoomService
    private User user;
    private User friendUser;

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
    private FriendServiceTest(FriendService friendService, UserRepository userRepo) {
        this.friendService = friendService;
        this.userRepo = userRepo;
    }

    @Test
    @Order(1)
    @DisplayName("내 편에서 친구[X] 상대편에서 친구[X]")
    @Transactional
    void bothNotFriend() {
        /* 0-1. Set base data */
        setBaseData();

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friendUser.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friendUser.getId(), user.getId());

        /* 1. Request friend */
        RequestFriendDto.Response requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();

        /* 2. Accept friend */
        AcceptFriendDto.Response insertRsp = friendService.acceptFriend(acceptReq);
        assertThat(insertRsp).isNotNull();

        /* 2. Find friends */
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friendUser.getId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 3. Delete friend */
        // 1) Delete from my side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friendUser.getId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friendUser.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friendUser.getId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friendUser.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friendUser.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friendUser.getId());

        /* 0-3. Request friend for other side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Accept friend for other side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete friend from my side */
        // Delete from user side
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(user.getId(), friendUser.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // My side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(user.getId());
        assertThat(beforeMySideList.size()).isZero();

        // Friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(beforeFriendSideList.size()).isOne();

        FindFriendsDto.Response beforeFriendSideFindRsp = beforeFriendSideList.get(0);
        assertThat(beforeFriendSideFindRsp).isNotNull();
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(beforeFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(beforeFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(beforeFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(beforeFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friendUser.getId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 5. Delete friend */
        // 1) Delete from user side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friendUser.getId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friendUser.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friendUser.getId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friendUser.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friendUser.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friendUser.getId());
        PatchFriendStatusDto.Request blockReq = new PatchFriendStatusDto.Request(friendUser.getId(), user.getId(), FriendStatus.BLOCK);
        CancelFriendDto.Request cancelReq = new CancelFriendDto.Request(user.getId(), friendUser.getId());

        /* 0-3. Request friend for other side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Accept friend for other side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete friend from my side */
        // Delete from user side
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(user.getId(), friendUser.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideFriends = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(beforeFriendSideList.size()).isOne();

        FindFriendsDto.Response beforeFriendSideFindRsp = beforeFriendSideList.get(0);
        assertThat(beforeFriendSideFindRsp).isNotNull();
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(beforeFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(beforeFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(beforeFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(beforeFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertThat(mySideFindList.size()).isOne();

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.WAIT);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friendUser.getId());
        assertThat(friendSideFindList).isNotNull();
        assertFalse(friendSideFindList.isEmpty());

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        /* 5. Delete friend */
        // 1) Delete from user side
        Long userId = user.getId();
        Long friendId = friendUser.getId();
        assertThrows(
                FriendInternalServerException.class, () -> friendService.deleteFriend(userId, friendId)
        );

        // Cancel friend request
        CancelFriendDto.Response cancelRsp = friendService.cancelFriend(cancelReq);
        assertThat(cancelRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList  = friendService.findFriends(friendUser.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friendUser.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friendUser.getId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friendUser.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friendUser.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friendUser.getId());

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Add friend for my side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete user from friend side */
        // Delete user from friend
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(friendUser.getId(), user.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(user.getId());
        assertThat(beforeMySideList.size()).isOne();

        FindFriendsDto.Response beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(beforeMySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(beforeMySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(beforeMySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(beforeMySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(beforeMySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friendUser.getId());
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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friendUser.getId());
        assertThat(friendSideFindList).isNotNull();
        assertTrue(friendSideFindList.isEmpty());

        /* 5. Delete friend */
        // 1) Delete from user side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friendUser.getId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // 2) Delete from friend side
        Long userId = user.getId();
        Long friendId = friendUser.getId();
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendId, userId)
        );

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friendUser.getId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friendUser.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friendUser.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friendUser.getId());
        PatchFriendStatusDto.Request blockReq = new PatchFriendStatusDto.Request(user.getId(), friendUser.getId(), FriendStatus.BLOCK);

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();

        /* 0-4. Add friend for my side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();

        /* 0-5. Delete user from friend side */
        // Delete user from friend
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(friendUser.getId(), user.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(user.getId());
        assertThat(beforeMySideList.size()).isOne();

        FindFriendsDto.Response beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(beforeMySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(beforeMySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(beforeMySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(beforeMySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(beforeMySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friendUser.getId());
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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friendUser.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isZero();

        /* 4. Delete friend */
        // 1) Delete from user side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friendUser.getId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // 2) Delete from friend side
        Long userId = user.getId();
        Long friendId = friendUser.getId();
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendId, userId)
        );

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friendUser.getId());
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
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friendUser.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friendUser.getId(), user.getId());

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
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertFalse(mySideFindList.isEmpty());

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friendUser.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friendUser.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friendUser.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friendUser.isBirthdayOpen() ? friendUser.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friendUser.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friendUser.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isOne();

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 4. Delete friend */
        // 1) Delete from user side
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friendUser.getId());
        assertThat(mySideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isOne();

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // 2) Delete from friend side
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friendUser.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friendUser.getId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    // life cycle: @Before -> @Test => separate => Not maintained @Transactional
    // Call function in @Test function => maintained @Transactional
    void setBaseData() {
        /* 0-1. Create user, friend */
        user = userRepo.save(User.builder()
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
                .build());

        friendUser = userRepo.save(User.builder()
                .snsId("Friend1_snsId")
                .snsType(2)
                .pushToken("Friend1 Token")
                .name("Friend1")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs(DeviceOs.IOS)
                .role(UserRole.USER)
                .build());
    }
}
