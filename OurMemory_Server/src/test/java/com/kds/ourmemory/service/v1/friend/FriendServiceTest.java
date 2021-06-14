package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.controller.v1.friend.dto.DeleteFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.InsertFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.RequestFriendDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendServiceTest {
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
     * ______________________________________________
     * |My side friend|Friend side friend|Add Friend|
     * |============================================|
     * |       X      |         X       |    Both   |
     * |       X      |         O       |  My side  |
     * |       O      |         X       |Friend side|    Not yet.
     * |       O      |         O       | Exception |   Not yet.
     * ----------------------------------------------
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
    void MySideX_FriendSideX_Friend_Request_Create_Read_Delete() {
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
                .deviceOs("Android")
                .build());

        User friend1 = userRepo.save(User.builder()
                .snsId("Friend1_snsId")
                .snsType(2)
                .pushToken("Friend1 Token")
                .name("Friend1")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("iOS")
                .build());

        User friend2 = userRepo.save(User.builder()
                .snsId("Friend2_snsId")
                .snsType(1)
                .pushToken("Friend2 Token")
                .name("Friend2")
                .birthday("0907")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("Android")
                .build());

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq1_MySideX_FriendSideX = new RequestFriendDto.Request(user.getId(), friend1.getId());
        RequestFriendDto.Request requestReq2_MySideX_FriendSideX = new RequestFriendDto.Request(user.getId(), friend2.getId());

        InsertFriendDto.Request insertReq1_MySideX_FriendSideX = new InsertFriendDto.Request(user.getId(), friend1.getId());
        InsertFriendDto.Request insertReq2_MySideX_FriendSideX = new InsertFriendDto.Request(user.getId(), friend2.getId());


        /* 1. Request friends */
        RequestFriendDto.Response requestRsp1_MySideX_FriendSideX = friendService.requestFriend(requestReq1_MySideX_FriendSideX);
        assertThat(requestRsp1_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(requestRsp1_MySideX_FriendSideX.getRequestDate())).isTrue();
        log.debug("requestDate: {}", requestRsp1_MySideX_FriendSideX.getRequestDate());

        RequestFriendDto.Response requestRsp2_MySideX_FriendSideX = friendService.requestFriend(requestReq2_MySideX_FriendSideX);
        assertThat(requestRsp2_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(requestRsp2_MySideX_FriendSideX.getRequestDate())).isTrue();
        log.debug("requestDate: {}", requestRsp2_MySideX_FriendSideX.getRequestDate());

        /* 2. Add friends */
        InsertFriendDto.Response insertRsp1_MySideX_FriendSideX = friendService.addFriend(insertReq1_MySideX_FriendSideX);
        assertThat(insertRsp1_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(insertRsp1_MySideX_FriendSideX.getAddDate())).isTrue();
        log.debug("addDate: {}", insertRsp1_MySideX_FriendSideX.getAddDate());

        InsertFriendDto.Response insertRsp2_MySideX_FriendSideX = friendService.addFriend(insertReq2_MySideX_FriendSideX);
        assertThat(insertRsp2_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(insertRsp2_MySideX_FriendSideX.getAddDate())).isTrue();
        log.debug("addDate: {}", insertRsp2_MySideX_FriendSideX.getAddDate());

        /* 2. Find friends */
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        boolean isOne = false;
        boolean isTwo = false;
        for (Friend friend: responseList) {
            if (friend.getFriend().getId().equals(friend1.getId())) isOne = true;
            if (friend.getFriend().getId().equals(friend2.getId())) isTwo = true;
        }
        assertThat(isOne && isTwo).isTrue();

        responseList = friendService.findFriends(friend1.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getFriend().getId().equals(user.getId())).isTrue();

        responseList = friendService.findFriends(friend2.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getFriend().getId().equals(user.getId())).isTrue();

        /* 3-1. Delete friend1 */
        // Delete friend1 from user
        DeleteFriendDto.Response mySideDeleteRsp1_MySideX_FriendSideX = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend1.getId()));
        assertThat(mySideDeleteRsp1_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(mySideDeleteRsp1_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends1 = friendService.findFriends(user.getId());
        assertThat(mySideFriends1.size()).isEqualTo(1);
        Friend mySideFriend1 = mySideFriends1.get(0);
        assertThat(mySideFriend1.getFriend()).isEqualTo(friend2);

        // Check friend side
        List<Friend> friendSideFriends1 = friendService.findFriends(friend1.getId());
        assertThat(friendSideFriends1.size()).isEqualTo(1);
        Friend friendSideFriend1 = friendSideFriends1.get(0);
        assertThat(friendSideFriend1.getFriend().getId()).isEqualTo(user.getId());

        // Delete user from friend1
        DeleteFriendDto.Response friendSideDeleteRsp1_MySideX_FriendSideX = friendService.delete(friend1.getId(),
                new DeleteFriendDto.Request(user.getId()));
        assertThat(friendSideDeleteRsp1_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(friendSideDeleteRsp1_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends1 = friendService.findFriends(user.getId());
        assertThat(mySideFriends1.size()).isEqualTo(1);
        mySideFriend1 = mySideFriends1.get(0);
        assertThat(mySideFriend1.getFriend()).isEqualTo(friend2);

        // Check friend side
        friendSideFriends1 = friendService.findFriends(friend1.getId());
        assertThat(friendSideFriends1.size()).isEqualTo(0);


        /* 3-2. Delete friend2 */
        // Delete friend2 from user
        DeleteFriendDto.Response mySideDeleteRsp2_MySideX_FriendSideX = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend2.getId()));
        assertThat(mySideDeleteRsp2_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(mySideDeleteRsp2_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends2 = friendService.findFriends(user.getId());
        assertThat(mySideFriends2.size()).isEqualTo(0);

        // Check friend side
        List<Friend> friendSideFriends2 = friendService.findFriends(friend2.getId());
        assertThat(friendSideFriends2.size()).isEqualTo(1);
        Friend friendSideFriend2 = friendSideFriends2.get(0);
        assertThat(friendSideFriend2.getFriend().getId()).isEqualTo(user.getId());

        // Delete user from friend2
        DeleteFriendDto.Response friendSideDeleteRsp2_MySideX_FriendSideX = friendService.delete(friend2.getId(),
                new DeleteFriendDto.Request(user.getId()));
        assertThat(friendSideDeleteRsp2_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(friendSideDeleteRsp2_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends2 = friendService.findFriends(user.getId());
        assertThat(mySideFriends2.size()).isEqualTo(0);

        // Check friend side
        friendSideFriends2 = friendService.findFriends(friend1.getId());
        assertThat(friendSideFriends2.size()).isEqualTo(0);
    }

    @Test
    @Order(2)
    @Transactional
    void MySideX_FriendSideO_Friend_Request_Create_Read_Delete() throws IllegalArgumentException {
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
                .deviceOs("Android")
                .build());

        User friend1 = userRepo.save(User.builder()
                .snsId("Friend1_snsId")
                .snsType(2)
                .pushToken("Friend1 Token")
                .name("Friend1")
                .birthday("0519")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("iOS")
                .build());

        User friend2 = userRepo.save(User.builder()
                .snsId("Friend2_snsId")
                .snsType(1)
                .pushToken("Friend2 Token")
                .name("Friend2")
                .birthday("0907")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("Android")
                .build());

        /* 0-2. Create request */
        RequestFriendDto.Request requestReq1_MySideX_FriendSideO = new RequestFriendDto.Request(user.getId(), friend1.getId());
        RequestFriendDto.Request requestReq2_MySideX_FriendSideO = new RequestFriendDto.Request(user.getId(), friend2.getId());

        InsertFriendDto.Request insertReq1_MySideX_FriendSideO = new InsertFriendDto.Request(user.getId(), friend1.getId());
        InsertFriendDto.Request insertReq2_MySideX_FriendSideO = new InsertFriendDto.Request(user.getId(), friend2.getId());

        /* 0-3. Request friends for other side friends */
        RequestFriendDto.Response beforeRequestRsp1_MySideX_FriendSideO = friendService.requestFriend(requestReq1_MySideX_FriendSideO);
        assertThat(beforeRequestRsp1_MySideX_FriendSideO).isNotNull();
        assertThat(isNow(beforeRequestRsp1_MySideX_FriendSideO.getRequestDate())).isTrue();
        log.debug("requestDate: {}", beforeRequestRsp1_MySideX_FriendSideO.getRequestDate());

        RequestFriendDto.Response beforeRequestRsp2_MySideX_FriendSideO = friendService.requestFriend(requestReq2_MySideX_FriendSideO);
        assertThat(beforeRequestRsp2_MySideX_FriendSideO).isNotNull();
        assertThat(isNow(beforeRequestRsp2_MySideX_FriendSideO.getRequestDate())).isTrue();
        log.debug("requestDate: {}", beforeRequestRsp2_MySideX_FriendSideO.getRequestDate());

        /* 0-4. Add friends for other side friends */
        InsertFriendDto.Response beforeInsertRsp1_MySideX_FriendSideO = friendService.addFriend(insertReq1_MySideX_FriendSideO);
        assertThat(beforeInsertRsp1_MySideX_FriendSideO).isNotNull();
        assertThat(isNow(beforeInsertRsp1_MySideX_FriendSideO.getAddDate())).isTrue();
        log.debug("addDate: {}", beforeInsertRsp1_MySideX_FriendSideO.getAddDate());

        InsertFriendDto.Response beforeInsertRsp2_MySideX_FriendSideO = friendService.addFriend(insertReq2_MySideX_FriendSideO);
        assertThat(beforeInsertRsp2_MySideX_FriendSideO).isNotNull();
        assertThat(isNow(beforeInsertRsp2_MySideX_FriendSideO.getAddDate())).isTrue();
        log.debug("addDate: {}", beforeInsertRsp2_MySideX_FriendSideO.getAddDate());

        /* 0-5. Delete friend1 from my side */
        // Delete friend1 from user
        DeleteFriendDto.Response beforeMySideDeleteRsp1_MySideX_FriendSideX = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend1.getId()));
        assertThat(beforeMySideDeleteRsp1_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp1_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> beforeMySideFriends1 = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends1.size()).isEqualTo(1);
        Friend beforeMySideFriend1 = beforeMySideFriends1.get(0);
        assertThat(beforeMySideFriend1.getFriend()).isEqualTo(friend2);

        // Check friend side
        List<Friend> beforeFriendSideFriends1 = friendService.findFriends(friend1.getId());
        assertThat(beforeFriendSideFriends1.size()).isEqualTo(1);
        Friend beforeFriendSideFriend1 = beforeFriendSideFriends1.get(0);
        assertThat(beforeFriendSideFriend1.getFriend().getId()).isEqualTo(user.getId());

        /* 0-6. Delete friend2 from my side */
        // Delete friend2 from user
        DeleteFriendDto.Response beforeMySideDeleteRsp2_MySideX_FriendSideX = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend2.getId()));
        assertThat(beforeMySideDeleteRsp2_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(beforeMySideDeleteRsp2_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> beforeMySideFriends2 = friendService.findFriends(user.getId());
        assertThat(beforeMySideFriends2.size()).isEqualTo(0);

        // Check friend side
        List<Friend> beforeFriendSideFriends2 = friendService.findFriends(friend2.getId());
        assertThat(beforeFriendSideFriends2.size()).isEqualTo(1);
        Friend beforeFriendSideFriend2 = beforeFriendSideFriends2.get(0);
        assertThat(beforeFriendSideFriend2.getFriend().getId()).isEqualTo(user.getId());


        /* 1. Request friends */
        assertThrows(IllegalArgumentException.class, () ->
                friendService.requestFriend(requestReq1_MySideX_FriendSideO));

        assertThrows(IllegalArgumentException.class, () ->
                friendService.requestFriend(requestReq2_MySideX_FriendSideO));

        /* 2. Add friends */
        InsertFriendDto.Response insertRsp1_MySideX_FriendSideO = friendService.addFriend(insertReq1_MySideX_FriendSideO);
        assertThat(insertRsp1_MySideX_FriendSideO).isNotNull();
        assertThat(isNow(insertRsp1_MySideX_FriendSideO.getAddDate())).isTrue();
        log.debug("addDate: {}", insertRsp1_MySideX_FriendSideO.getAddDate());

        InsertFriendDto.Response insertRsp2_MySideX_FriendSideO = friendService.addFriend(insertReq2_MySideX_FriendSideO);
        assertThat(insertRsp2_MySideX_FriendSideO).isNotNull();
        assertThat(isNow(insertRsp2_MySideX_FriendSideO.getAddDate())).isTrue();
        log.debug("addDate: {}", insertRsp2_MySideX_FriendSideO.getAddDate());

        /* 2. Find friends */
        List<Friend> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        boolean isOne = false;
        boolean isTwo = false;
        for (Friend friend: responseList) {
            if (friend.getFriend().getId().equals(friend1.getId())) isOne = true;
            if (friend.getFriend().getId().equals(friend2.getId())) isTwo = true;
        }
        assertThat(isOne && isTwo).isTrue();

        responseList = friendService.findFriends(friend1.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getFriend().getId().equals(user.getId())).isTrue();

        responseList = friendService.findFriends(friend2.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getFriend().getId().equals(user.getId())).isTrue();

        /* 3-1. Delete friend1 */
        // Delete friend1 from user
        DeleteFriendDto.Response mySideDeleteRsp1_MySideX_FriendSideO = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend1.getId()));
        assertThat(mySideDeleteRsp1_MySideX_FriendSideO).isNotNull();
        assertThat(isNow(mySideDeleteRsp1_MySideX_FriendSideO.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends1 = friendService.findFriends(user.getId());
        assertThat(mySideFriends1.size()).isEqualTo(1);
        Friend mySideFriend1 = mySideFriends1.get(0);
        assertThat(mySideFriend1.getFriend()).isEqualTo(friend2);

        // Check friend side
        List<Friend> friendSideFriends1 = friendService.findFriends(friend1.getId());
        assertThat(friendSideFriends1.size()).isEqualTo(1);
        Friend friendSideFriend1 = friendSideFriends1.get(0);
        assertThat(friendSideFriend1.getFriend().getId()).isEqualTo(user.getId());

        // Delete user from friend1
        DeleteFriendDto.Response friendSideDeleteRsp1_MySideX_FriendSideX = friendService.delete(friend1.getId(),
                new DeleteFriendDto.Request(user.getId()));
        assertThat(friendSideDeleteRsp1_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(friendSideDeleteRsp1_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends1 = friendService.findFriends(user.getId());
        assertThat(mySideFriends1.size()).isEqualTo(1);
        mySideFriend1 = mySideFriends1.get(0);
        assertThat(mySideFriend1.getFriend()).isEqualTo(friend2);

        // Check friend side
        friendSideFriends1 = friendService.findFriends(friend1.getId());
        assertThat(friendSideFriends1.size()).isEqualTo(0);


        /* 3-2. Delete friend2 */
        // Delete friend2 from user
        DeleteFriendDto.Response mySideDeleteRsp2_MySideX_FriendSideX = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend2.getId()));
        assertThat(mySideDeleteRsp2_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(mySideDeleteRsp2_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        List<Friend> mySideFriends2 = friendService.findFriends(user.getId());
        assertThat(mySideFriends2.size()).isEqualTo(0);

        // Check friend side
        List<Friend> friendSideFriends2 = friendService.findFriends(friend2.getId());
        assertThat(friendSideFriends2.size()).isEqualTo(1);
        Friend friendSideFriend2 = friendSideFriends2.get(0);
        assertThat(friendSideFriend2.getFriend().getId()).isEqualTo(user.getId());

        // Delete user from friend2
        DeleteFriendDto.Response friendSideDeleteRsp2_MySideX_FriendSideX = friendService.delete(friend2.getId(),
                new DeleteFriendDto.Request(user.getId()));
        assertThat(friendSideDeleteRsp2_MySideX_FriendSideX).isNotNull();
        assertThat(isNow(friendSideDeleteRsp2_MySideX_FriendSideX.getDeleteDate())).isTrue();

        // Check my side
        mySideFriends2 = friendService.findFriends(user.getId());
        assertThat(mySideFriends2.size()).isEqualTo(1);
        Friend mySideFriend2 = mySideFriends2.get(0);
        assertThat(mySideFriend2.getFriend()).isEqualTo(friend2);

        // Check friend side
        friendSideFriends2 = friendService.findFriends(friend1.getId());
        assertThat(friendSideFriends2.size()).isEqualTo(0);
    }

    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
