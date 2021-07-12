package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.friend.dto.*;
import com.kds.ourmemory.entity.BaseTimeEntity;
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
    @DisplayName("내 편에서 친구[X] 상대편에서 친구[X]")
    @Transactional
    void bothNotFriend() {
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

        /* 1. Request friend */
        RequestFriendDto.Response requestRsp = friendService.requestFriend(requestReq);
        assertThat(requestRsp).isNotNull();
        assertThat(isNow(requestRsp.getRequestDate())).isTrue();

        /* 2. Accept friend */
        AcceptFriendDto.Response insertRsp = friendService.acceptFriend(acceptReq);
        assertThat(insertRsp).isNotNull();
        assertThat(isNow(insertRsp.getAcceptDate())).isTrue();

        /* 2. Find friends */
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertThat(!mySideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friend.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friend.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friend.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friend.isBirthdayOpen() ? friend.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friend.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friend.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(!friendSideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 3. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friend.getId());
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isEqualTo(1);

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friend.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(2)
    @DisplayName("내 편에서 친구[X] 상대편에서 친구[O] 차단[X]")
    @Transactional
    void onlyFriendSideAndNotBlock() {
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
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(user.getId(), friend.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // My side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(user.getId());
        assertThat(beforeMySideList.size()).isZero();

        // Friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friend.getId());
        assertThat(beforeFriendSideList.size()).isEqualTo(1);

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
        assertThat(isNow(reAddRsp_MySideX_FriendSideO_Block.getReAddDate())).isTrue();

        /* 4. Find friends */
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertThat(!mySideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friend.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friend.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friend.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friend.isBirthdayOpen() ? friend.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friend.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friend.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(!friendSideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 5. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friend.getId());
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isEqualTo(1);

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friend.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(3)
    @DisplayName("내 편에서 친구[X] 상대편에서 친구[O] 차단[O]")
    @Transactional
    void onlyFriendSideAndBlock() {
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
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(user.getId(), friend.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideFriends = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friend.getId());
        assertThat(beforeFriendSideList.size()).isEqualTo(1);

        FindFriendsDto.Response beforeFriendSideFindRsp = beforeFriendSideList.get(0);
        assertThat(beforeFriendSideFindRsp).isNotNull();
        assertThat(beforeFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(beforeFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(beforeFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(beforeFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(beforeFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(beforeFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

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
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertThat(mySideFindList.size()).isZero();

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friend.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(!friendSideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        /* 5. Delete friend */
        // Delete friend from user
        Long userId = user.getId();
        Long friendId = friend.getId();
        assertThrows(
                FriendInternalServerException.class, () -> friendService.deleteFriend(userId, friendId)
        );

        // Cancel friend request
        CancelFriendDto.Response cancelRsp = friendService.cancelFriend(cancelReq);
        assertThat(cancelRsp).isNotNull();
        assertThat(isNow(cancelRsp.getCancelDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList  = friendService.findFriends(friend.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isEqualTo(1);

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friend.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(4)
    @DisplayName("내 편에서 친구[O] 상대편에서 친구[X] 차단[X]")
    @Transactional
    void onlyMySideAndNotBlock() {
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
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(friend.getId(), user.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(user.getId());
        assertThat(beforeMySideList.size()).isEqualTo(1);

        FindFriendsDto.Response beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(friend.getId());
        assertThat(beforeMySideFindRsp.getName()).isEqualTo(friend.getName());
        assertThat(beforeMySideFindRsp.isBirthdayOpen()).isEqualTo(friend.isBirthdayOpen());
        assertThat(beforeMySideFindRsp.getBirthday()).isEqualTo(friend.isBirthdayOpen() ? friend.getBirthday() : null);
        assertThat(beforeMySideFindRsp.isSolar()).isEqualTo(friend.isSolar());
        assertThat(beforeMySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friend.getId());
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
        assertThat(!mySideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friend.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friend.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friend.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friend.isBirthdayOpen() ? friend.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friend.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friend.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.isEmpty()).isTrue();

        /* 5. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friend.getId());
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // Delete user from friend
        Long userId = user.getId();
        Long friendId = friend.getId();
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendId, userId)
        );

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(5)
    @DisplayName("내 편에서 친구[O] 상대편에서 친구[X] 차단[O]")
    @Transactional
    void onlyMySideAndBlock() {
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
        DeleteFriendDto.Response beforeMySideDeleteRsp = friendService.deleteFriend(friend.getId(), user.getId());
        assertThat(beforeMySideDeleteRsp).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> beforeMySideList = friendService.findFriends(user.getId());
        assertThat(beforeMySideList.size()).isEqualTo(1);

        FindFriendsDto.Response beforeMySideFindRsp = beforeMySideList.get(0);
        assertThat(beforeMySideFindRsp).isNotNull();
        assertThat(beforeMySideFindRsp.getFriendId()).isEqualTo(friend.getId());
        assertThat(beforeMySideFindRsp.getName()).isEqualTo(friend.getName());
        assertThat(beforeMySideFindRsp.isBirthdayOpen()).isEqualTo(friend.isBirthdayOpen());
        assertThat(beforeMySideFindRsp.getBirthday()).isEqualTo(friend.isBirthdayOpen() ? friend.getBirthday() : null);
        assertThat(beforeMySideFindRsp.isSolar()).isEqualTo(friend.isSolar());
        assertThat(beforeMySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Check friend side
        List<FindFriendsDto.Response> beforeFriendSideList = friendService.findFriends(friend.getId());
        assertThat(beforeFriendSideList.size()).isZero();

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
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertThat(!mySideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friend.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friend.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friend.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friend.isBirthdayOpen() ? friend.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friend.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.BLOCK);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friend.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isZero();

        /* 4. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friend.getId());
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isZero();

        // Delete user from friend
        Long userId = user.getId();
        Long friendId = friend.getId();
        assertThrows(
                FriendNotFoundFriendException.class, () -> friendService.deleteFriend(friendId, userId)
        );

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    @Test
    @Order(6)
    @DisplayName("내 편에서 친구[O] 상대편에서 친구[O]")
    @Transactional
    void bothAlreadyFriend() {
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
        // My side
        List<FindFriendsDto.Response> mySideFindList = friendService.findFriends(user.getId());
        assertThat(mySideFindList).isNotNull();
        assertThat(!mySideFindList.isEmpty()).isTrue();

        FindFriendsDto.Response mySideFindRsp = mySideFindList.get(0);
        assertThat(mySideFindRsp).isNotNull();
        assertThat(mySideFindRsp.getFriendId()).isEqualTo(friend.getId());
        assertThat(mySideFindRsp.getName()).isEqualTo(friend.getName());
        assertThat(mySideFindRsp.isBirthdayOpen()).isEqualTo(friend.isBirthdayOpen());
        assertThat(mySideFindRsp.getBirthday()).isEqualTo(friend.isBirthdayOpen() ? friend.getBirthday() : null);
        assertThat(mySideFindRsp.isSolar()).isEqualTo(friend.isSolar());
        assertThat(mySideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Friend side
        List<FindFriendsDto.Response> friendSideFindList = friendService.findFriends(friend.getId());
        assertThat(friendSideFindList).isNotNull();
        assertThat(friendSideFindList.size()).isEqualTo(1);

        FindFriendsDto.Response friendSideFindRsp = friendSideFindList.get(0);
        assertThat(friendSideFindRsp).isNotNull();
        assertThat(friendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(friendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(friendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(friendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(friendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(friendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        /* 4. Delete friend */
        // Delete friend from user
        DeleteFriendDto.Response mySideDeleteRsp = friendService.deleteFriend(user.getId(), friend.getId());
        assertThat(mySideDeleteRsp).isNotNull();
        assertThat(isNow(mySideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromMySideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromMySideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromMySideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromMySideFriendSideList.size()).isEqualTo(1);

        FindFriendsDto.Response deleteFriendSideFindRsp = deleteFromMySideFriendSideList.get(0);
        assertThat(deleteFriendSideFindRsp).isNotNull();
        assertThat(deleteFriendSideFindRsp.getFriendId()).isEqualTo(user.getId());
        assertThat(deleteFriendSideFindRsp.getName()).isEqualTo(user.getName());
        assertThat(deleteFriendSideFindRsp.isBirthdayOpen()).isEqualTo(user.isBirthdayOpen());
        assertThat(deleteFriendSideFindRsp.getBirthday()).isEqualTo(user.isBirthdayOpen() ? user.getBirthday() : null);
        assertThat(deleteFriendSideFindRsp.isSolar()).isEqualTo(user.isSolar());
        assertThat(deleteFriendSideFindRsp.getStatus()).isEqualTo(FriendStatus.FRIEND);

        // Delete user from friend
        DeleteFriendDto.Response friendSideDeleteRsp = friendService.deleteFriend(friend.getId(), user.getId());
        assertThat(friendSideDeleteRsp).isNotNull();
        assertThat(isNow(friendSideDeleteRsp.getDeleteDate())).isTrue();

        // Check my side
        List<FindFriendsDto.Response> deleteFromFriendSideMySideList = friendService.findFriends(user.getId());
        assertThat(deleteFromFriendSideMySideList.size()).isZero();

        // Check friend side
        List<FindFriendsDto.Response> deleteFromFriendSideFriendSideList = friendService.findFriends(friend.getId());
        assertThat(deleteFromFriendSideFriendSideList.size()).isZero();
    }

    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
