package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.friend.dto.*;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
import com.kds.ourmemory.repository.user.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FriendServiceTest {
    private final FriendService friendService;

    // Add to work with user data
    private final UserRepository userRepo;

    /**
     * Assert time format -> delete sec
     *
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;

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

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @Transactional
    void MySideX_FriendSideX_BlockX() {
        /* 0-1. Create user, friend */
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
                .build());

        User friend = userRepo.save(User.builder()
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

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friend.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friend.getId(), user.getId());

        DeleteFriendDto.Request deleteReqFriendFromUser = new DeleteFriendDto.Request(user.getId(), friend.getId());
        DeleteFriendDto.Request deleteReqUserFromFriend = new DeleteFriendDto.Request(friend.getId(), user.getId());


        /* 1. Request friend */
        RequestFriendDto.Response requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
        assertThat(isNow(requestRsp.getRequestDate())).isTrue();

        /* 2. Accept friend */
        AcceptFriendDto.Response insertRsp = friendService.acceptFriend(acceptReq);
        assertThat(insertRsp).isNotNull();
        assertThat(isNow(insertRsp.getAcceptDate())).isTrue();

        /* 2. Find friends */
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        Friend findFriend = responseList.get(0);
        assertThat(findFriend.getFriend()).isEqualTo(friend);

        responseList = friendService.findFriends(friend.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getFriend().getId()).isEqualTo(user.getId());

        /* 3. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(deleteReqFriendFromUser);
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        List<Friend> friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isEqualTo(1);
        Friend friendSideFriend = friendSideFriends.get(0);
        assertThat(friendSideFriend.getFriend().getId()).isEqualTo(user.getId());

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(deleteReqUserFromFriend);
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();
    }

    @Test
    @Order(2)
    @Transactional
    void MySideX_FriendSideO_BlockX() {
        /* 0-1. Create user, friend */
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
                .build());

        User friend = userRepo.save(User.builder()
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

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friend.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friend.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friend.getId());

        DeleteFriendDto.Request deleteReqFriendFromUser = new DeleteFriendDto.Request(user.getId(), friend.getId());
        DeleteFriendDto.Request deleteReqUserFromFriend = new DeleteFriendDto.Request(friend.getId(), user.getId());

        /* 0-3. Request friend for other side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(isNow(beforeRequestRsp.getRequestDate())).isTrue();

        /* 0-4. Accept friend for other side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(isNow(beforeInsertRsp.getAcceptDate())).isTrue();

        /* 0-5. Delete friend from my side */
        // Delete friend from user
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(deleteReqFriendFromUser);
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> beforeMySideFriends = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends.size()).isZero();

        // Check friend side
        List<Friend> beforeFriendSideFriends = friendService.findFriends(friend.getId());
        assertThat(beforeFriendSideFriends.size()).isEqualTo(1);
        Friend beforeFriendSideFriend = beforeFriendSideFriends.get(0);
        assertThat(beforeFriendSideFriend.getFriend().getId()).isEqualTo(user.getId());


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
        assertThat(isNow(reAddRsp_MySideX_FriendSideO_Block.getReAddDate())).isTrue();

        /* 4. Find friends */
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        Friend findFriend = responseList.get(0);
        assertThat(findFriend.getFriend()).isEqualTo(friend);

        responseList = friendService.findFriends(friend.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getFriend().getId()).isEqualTo(user.getId());

        /* 5. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(deleteReqFriendFromUser);
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        List<Friend> friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isEqualTo(1);
        Friend friendSideFriend = friendSideFriends.get(0);
        assertThat(friendSideFriend.getFriend().getId()).isEqualTo(user.getId());

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(deleteReqUserFromFriend);
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();
    }

    @Test
    @Order(3)
    @Transactional
    void MySideX_FriendSideO_BlockO() {
        /* 0-1. Create user, friend */
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
                .build());

        User friend = userRepo.save(User.builder()
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

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friend.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friend.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friend.getId());
        PatchFriendStatusDto.Request blockReq = new PatchFriendStatusDto.Request(friend.getId(), user.getId(), FriendStatus.BLOCK);
        CancelFriendDto.Request cancelReq = new CancelFriendDto.Request(user.getId(), friend.getId());

        DeleteFriendDto.Request deleteReqFriendFromUser = new DeleteFriendDto.Request(user.getId(), friend.getId());
        DeleteFriendDto.Request deleteReqUserFromFriend = new DeleteFriendDto.Request(friend.getId(), user.getId());

        /* 0-3. Request friend for other side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(isNow(beforeRequestRsp.getRequestDate())).isTrue();

        /* 0-4. Accept friend for other side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(isNow(beforeInsertRsp.getAcceptDate())).isTrue();

        /* 0-5. Delete friend from my side */
        // Delete friend from user
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(deleteReqFriendFromUser);
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> beforeMySideFriends = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends.size()).isZero();

        // Check friend side
        List<Friend> beforeFriendSideFriends = friendService.findFriends(friend.getId());
        assertThat(beforeFriendSideFriends.size()).isEqualTo(1);
        Friend beforeFriendSideFriend = beforeFriendSideFriends.get(0);
        assertThat(beforeFriendSideFriend.getFriend().getId()).isEqualTo(user.getId());

        /* 0-6. Block friend from friend side */
        PatchFriendStatusDto.Response beforeFriendSideBlockRsp = friendService.patchFriendStatus(blockReq);
        assertThat(beforeFriendSideBlockRsp).isNotNull();
        assertThat(isNow(beforeFriendSideBlockRsp.getPatchDate())).isTrue();


        /* 1. Request friend */
        RequestFriendDto.Response requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
        assertThat(isNow(requestRsp.getRequestDate())).isTrue();

        /* 2. Accept friend */
        assertThrows(
                FriendStatusException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Add friend */
        assertThrows(
                FriendBlockedException.class, () -> friendService.reAddFriend(addReq)
        );

        /* 4. Find friends */
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(responseList.size()).isZero();

        responseList = friendService.findFriends(friend.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        Friend findFriend = responseList.get(0);
        assertThat(findFriend.getStatus()).isEqualTo(FriendStatus.BLOCK);
        assertThat(findFriend.getFriend()).isEqualTo(user);

        /* 5. Delete friend */
        // Delete friend from user
        assertThrows(
                FriendInternalServerException.class, () -> friendService.deleteFriend(deleteReqFriendFromUser)
        );

        // Cancel friend request
        CancelFriendDto.Response cancelRsp = friendService.cancelFriend(cancelReq);
        assertThat(cancelRsp).isNotNull();
        assertThat(isNow(cancelRsp.getCancelDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        List<Friend> friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isEqualTo(1);
        findFriend = friendSideFriends.get(0);
        assertThat(findFriend.getFriend()).isEqualTo(user);
        assertThat(findFriend.getStatus()).isEqualTo(FriendStatus.BLOCK);

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(deleteReqUserFromFriend);
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();
    }

    @Test
    @Order(4)
    @Transactional
    void MySideO_FriendSideX_BlockX() {
        /* 0-1. Create user, friend */
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
                .build());

        User friend = userRepo.save(User.builder()
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

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friend.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friend.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friend.getId());

        DeleteFriendDto.Request deleteReqFriendFromUser = new DeleteFriendDto.Request(user.getId(), friend.getId());
        DeleteFriendDto.Request deleteReqUserFromFriend = new DeleteFriendDto.Request(friend.getId(), user.getId());

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(isNow(beforeRequestRsp.getRequestDate())).isTrue();

        /* 0-4. Add friend for my side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(isNow(beforeInsertRsp.getAcceptDate())).isTrue();

        /* 0-5. Delete user from friend side */
        // Delete user from friend
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(deleteReqUserFromFriend);
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> beforeMySideFriends = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends.size()).isEqualTo(1);

        // Check friend side
        List<Friend> beforeFriendSideFriends = friendService.findFriends(friend.getId());
        assertThat(beforeFriendSideFriends.size()).isZero();


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
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        Friend findFriend = responseList.get(0);
        assertThat(findFriend.getFriend()).isEqualTo(friend);

        responseList = friendService.findFriends(friend.getId());
        assertThat(responseList).isNotNull();
        assertThat(responseList.isEmpty()).isTrue();

        /* 5. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(deleteReqFriendFromUser);
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        List<Friend> friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();

        // Delete user from friend
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(deleteReqUserFromFriend)
        );

        // Check my side
        mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();
    }

    @Test
    @Order(5)
    @Transactional
    void MySideO_FriendSideX_BlockO() {
        /* 0-1. Create user, friend */
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
                .build());

        User friend = userRepo.save(User.builder()
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

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friend.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friend.getId(), user.getId());
        ReAddFriendDto.Request addReq = new ReAddFriendDto.Request(user.getId(), friend.getId());
        PatchFriendStatusDto.Request blockReq = new PatchFriendStatusDto.Request(user.getId(), friend.getId(), FriendStatus.BLOCK);

        DeleteFriendDto.Request deleteReqFriendFromUser = new DeleteFriendDto.Request(user.getId(), friend.getId());
        DeleteFriendDto.Request deleteReqUserFromFriend = new DeleteFriendDto.Request(friend.getId(), user.getId());

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(isNow(beforeRequestRsp.getRequestDate())).isTrue();

        /* 0-4. Add friend for my side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(isNow(beforeInsertRsp.getAcceptDate())).isTrue();

        /* 0-5. Delete user from friend side */
        // Delete user from friend
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(deleteReqUserFromFriend);
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> beforeMySideFriends = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends.size()).isEqualTo(1);

        // Check friend side
        List<Friend> beforeFriendSideFriends = friendService.findFriends(friend.getId());
        assertThat(beforeFriendSideFriends.size()).isZero();

        /* 0-6. Block friend from my side */
        PatchFriendStatusDto.Response beforeMySideBlockRsp = friendService.patchFriendStatus(blockReq);
        assertThat(beforeMySideBlockRsp).isNotNull();
        assertThat(isNow(beforeMySideBlockRsp.getPatchDate())).isTrue();


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
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        Friend findFriend = responseList.get(0);
        assertThat(findFriend.getStatus()).isEqualTo(FriendStatus.BLOCK);
        assertThat(findFriend.getFriend()).isEqualTo(friend);

        responseList = friendService.findFriends(friend.getId());
        assertThat(responseList).isNotNull();
        assertThat(responseList.size()).isZero();

        /* 4. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(deleteReqFriendFromUser);
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        List<Friend> friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();

        // Delete user from friend
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(deleteReqUserFromFriend)
        );

        // Check my side
        mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();
    }

    @Test
    @Order(6)
    @Transactional
    void MySideO_FriendSideO_BlockX() {
        /* 0-1. Create user, friends */
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
                .build());

        User friend = userRepo.save(User.builder()
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

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq = new RequestFriendDto.Request(user.getId(), friend.getId());
        AcceptFriendDto.Request acceptReq = new AcceptFriendDto.Request(friend.getId(), user.getId());

        DeleteFriendDto.Request deleteReqFriendFromUser = new DeleteFriendDto.Request(user.getId(), friend.getId());
        DeleteFriendDto.Request deleteReqUserFromFriend = new DeleteFriendDto.Request(friend.getId(), user.getId());

        /* 0-3. Request friend for my side friend */
        RequestFriendDto.Response beforeRequestRsp = friendService.requestFriend(requestReq);
        assertThat(beforeRequestRsp).isNotNull();
        assertThat(isNow(beforeRequestRsp.getRequestDate())).isTrue();

        /* 0-4. Add friend for both side friend */
        AcceptFriendDto.Response beforeInsertRsp = friendService.acceptFriend(acceptReq);
        assertThat(beforeInsertRsp).isNotNull();
        assertThat(isNow(beforeInsertRsp.getAcceptDate())).isTrue();


        /* 1. Request friend */
        assertThrows(
            IllegalArgumentException.class, () -> friendService.requestFriend(requestReq)
        );

        /* 2. Accept friend */
        assertThrows(
                FriendStatusException.class, () -> friendService.acceptFriend(acceptReq)
        );

        /* 3. Find friend */
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        Friend findFriend = responseList.get(0);
        assertThat(findFriend.getFriend()).isEqualTo(friend);

        responseList = friendService.findFriends(friend.getId());
        assertThat(responseList).isNotNull();
        assertThat(responseList.size()).isEqualTo(1);
        findFriend = responseList.get(0);
        assertThat(findFriend.getFriend()).isEqualTo(user);


        /* 4. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(deleteReqFriendFromUser);
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        List<Friend> friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isEqualTo(1);
        Friend friendSideFriend = friendSideFriends.get(0);
        assertThat(friendSideFriend.getFriend().getId()).isEqualTo(user.getId());

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(deleteReqUserFromFriend);
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends = friendService.findFriends(user.getId());
        assertThat(mySideFriends.size()).isZero();

        // Check friend side
        friendSideFriends = friendService.findFriends(friend.getId());
        assertThat(friendSideFriends.size()).isZero();
    }

    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
