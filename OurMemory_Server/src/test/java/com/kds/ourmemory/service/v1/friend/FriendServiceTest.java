package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.controller.v1.friend.dto.InsertFriendDto;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendServiceTest {
    private final FriendService friendService;
    
    private final UserRepository userRepo;  // Add to work with user data

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
    void Friend_Create_Read() {
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
                .snsType(2)
                .pushToken("Friend2 Token")
                .name("Friend2")
                .birthday("0807")
                .solar(true)
                .birthdayOpen(true)
                .used(true)
                .deviceOs("iOS")
                .build());

        /* 0-2. Create request */
        List<Long> friendsId = new ArrayList<>();
        friendsId.add(friend1.getId());
        friendsId.add(friend2.getId());
        InsertFriendDto.Request insertFriendRequest = new InsertFriendDto.Request(friendsId);

        /* 1. Add friends */
        InsertFriendDto.Response insertFriendResponse = friendService.addFriend(user.getId(), insertFriendRequest);
        assertThat(insertFriendResponse).isNotNull();
        assertThat(isNow(insertFriendResponse.getAddDate())).isTrue();

        log.debug("addDate: {}", insertFriendResponse.getAddDate());

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
    }

    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
