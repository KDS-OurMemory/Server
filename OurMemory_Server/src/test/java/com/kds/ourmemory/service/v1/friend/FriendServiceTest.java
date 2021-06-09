package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.controller.v1.friend.dto.DeleteFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.InsertFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.RequestFriendDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
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
    void Friend_Request_Create_Read_Delete() {
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
        RequestFriendDto.Request requestFriendRequest1 = new RequestFriendDto.Request(user.getId(), friend1.getId());
        RequestFriendDto.Request requestFriendRequest2 = new RequestFriendDto.Request(user.getId(), friend2.getId());

        InsertFriendDto.Request insertFriendRequest1 = new InsertFriendDto.Request(user.getId(), friend1.getId());
        InsertFriendDto.Request insertFriendRequest2 = new InsertFriendDto.Request(user.getId(), friend2.getId());


        /* 1. Request friends */
        RequestFriendDto.Response requestFriendResponse1 = friendService.requestFriend(requestFriendRequest1);
        assertThat(requestFriendResponse1).isNotNull();
        assertThat(isNow(requestFriendResponse1.getRequestDate())).isTrue();
        log.debug("requestDate: {}", requestFriendResponse1.getRequestDate());

        RequestFriendDto.Response requestFriendResponse2 = friendService.requestFriend(requestFriendRequest2);
        assertThat(requestFriendResponse2).isNotNull();
        assertThat(isNow(requestFriendResponse2.getRequestDate())).isTrue();
        log.debug("requestDate: {}", requestFriendResponse2.getRequestDate());

        /* 2. Add friends */
        InsertFriendDto.Response insertFriendResponse1 = friendService.addFriend(insertFriendRequest1);
        assertThat(insertFriendResponse1).isNotNull();
        assertThat(isNow(insertFriendResponse1.getAddDate())).isTrue();
        log.debug("addDate: {}", insertFriendResponse1.getAddDate());

        InsertFriendDto.Response insertFriendResponse2 = friendService.addFriend(insertFriendRequest2);
        assertThat(insertFriendResponse2).isNotNull();
        assertThat(isNow(insertFriendResponse2.getAddDate())).isTrue();
        log.debug("addDate: {}", insertFriendResponse2.getAddDate());

        /* 2. Find friends */
        List<User> responseList = friendService.findFriends(user.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        boolean isOne = false;
        boolean isTwo = false;
        for (User friend: responseList) {
            if (friend.getId().equals(friend1.getId())) isOne = true;
            if (friend.getId().equals(friend2.getId())) isTwo = true;
        }
        assertThat(isOne && isTwo).isTrue();

        responseList = friendService.findFriends(friend1.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getId().equals(user.getId())).isTrue();

        responseList = friendService.findFriends(friend2.getId());
        assertThat(responseList).isNotNull();
        assertThat(!responseList.isEmpty()).isTrue();
        assertThat(responseList.get(0).getId().equals(user.getId())).isTrue();

        /* 3. Delete friends */
        DeleteFriendDto.Response deleteFriendResponseDto1 = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend1.getId()));
        assertThat(deleteFriendResponseDto1).isNotNull();
        assertThat(isNow(deleteFriendResponseDto1.getDeleteDate())).isTrue();

        List<User> firstDeleteFriends = friendService.findFriends(user.getId());
        assertThat(firstDeleteFriends.size()).isEqualTo(1);
        User secondFriend = firstDeleteFriends.get(0);
        assertThat(secondFriend).isEqualTo(friend2);

        DeleteFriendDto.Response deleteFriendResponseDto2 = friendService.delete(user.getId(),
                new DeleteFriendDto.Request(friend2.getId()));
        assertThat(deleteFriendResponseDto2).isNotNull();
        assertThat(isNow(deleteFriendResponseDto2.getDeleteDate())).isTrue();

        List<User> secondDeleteFriends = friendService.findFriends(user.getId());
        assertThat(secondDeleteFriends.size()).isEqualTo(0);
    }

    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
