package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchTokenDto;
import com.kds.ourmemory.controller.v1.user.dto.UpdateUserDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.DeviceOs;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    private final UserService userService;

    /**
     * Assert time format -> delete sec
     *
     * This is because time difference occurs after room creation due to relation table work.
     */
    private DateTimeFormatter format;

    @Autowired
    private UserServiceTest(UserService userService) {
        this.userService = userService;
    }

    @BeforeAll
    void setUp() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    }

    @Test
    @Order(1)
    @DisplayName("회원가입-로그인-단일 조회-토큰변경-업데이트")
    @Transactional
    void signUpSignInFind() {
        /* 0. Create Request */
        var insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );
        var patchReq = new PatchTokenDto.Request("patch token");
        var updateReq = new UpdateUserDto.Request("update name", "0927", false, false);

        /* 1. Insert */
        var insertRsp = userService.signUp(insertReq);
        assertThat(insertRsp).isNotNull();
        assertThat(insertRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insertRsp.getJoinDate())).isTrue();

        /* 2. Sign in */
        var signInRsp = userService.signIn(insertReq.getSnsType(), insertReq.getSnsId());
        assertThat(signInRsp).isNotNull();
        assertThat(signInRsp.getUserId()).isEqualTo(insertRsp.getUserId());
        assertThat(signInRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(signInRsp.getPushToken()).isEqualTo(insertReq.getPushToken());
        assertThat(signInRsp.getBirthday()).isEqualTo(insertReq.getBirthday());

        /* 3. Find */
        var findRsp = userService.find(insertRsp.getUserId());
        assertThat(findRsp).isNotNull();
        assertThat(findRsp.getId()).isEqualTo(insertRsp.getUserId());
        assertThat(findRsp.getSnsType()).isEqualTo(insertReq.getSnsType());
        assertThat(findRsp.getSnsId()).isEqualTo(insertReq.getSnsId());
        assertThat(findRsp.getName()).isEqualTo(insertReq.getName());
        assertThat(findRsp.getBirthday()).isEqualTo(insertReq.getBirthday());
        assertThat(findRsp.isSolar()).isEqualTo(insertReq.isSolar());
        assertThat(findRsp.isBirthdayOpen()).isEqualTo(insertReq.isBirthdayOpen());
        assertThat(findRsp.getPushToken()).isEqualTo(insertReq.getPushToken());

        /* 4. Patch token */
        var patchRsp = userService.patchToken(insertRsp.getUserId(), patchReq);
        assertThat(patchRsp).isNotNull();
        assertThat(isNow(patchRsp.getPatchDate())).isTrue();

        /* 5. Find after patch */
        var afterPatchFindRsp = userService.find(insertRsp.getUserId());
        assertThat(afterPatchFindRsp.getPushToken()).isEqualTo(patchReq.getPushToken());

        /* 6. Update */
        var updateRsp = userService.update(insertRsp.getUserId(), updateReq);
        assertThat(updateRsp).isNotNull();
        assertThat(isNow(updateRsp.getUpdateDate())).isTrue();

        /* 7. Find after update */
        var afterUpdateFindRsp = userService.find(insertRsp.getUserId());
        assertThat(afterUpdateFindRsp.getName()).isEqualTo(updateReq.getName());
        assertThat(afterUpdateFindRsp.getBirthday()).isEqualTo(updateReq.getBirthday());
        assertThat(afterUpdateFindRsp.isBirthdayOpen()).isEqualTo(updateReq.getBirthdayOpen());
        assertThat(afterUpdateFindRsp.isPush()).isEqualTo(updateReq.getPush());
    }

    @Test
    @Order(2)
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
        assertThat(insertUniqueNameRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insertUniqueNameRsp.getJoinDate())).isTrue();

        var insertSameNameRsp1 = userService.signUp(insertSameNameReq1);
        assertThat(insertSameNameRsp1).isNotNull();
        assertThat(insertSameNameRsp1.getJoinDate()).isNotNull();
        assertThat(isNow(insertSameNameRsp1.getJoinDate())).isTrue();

        var insertSameNameRsp2 = userService.signUp(insertSameNameReq2);
        assertThat(insertSameNameRsp2).isNotNull();
        assertThat(insertSameNameRsp2.getJoinDate()).isNotNull();
        assertThat(isNow(insertSameNameRsp2.getJoinDate())).isTrue();
        
        /* 2. Find users */
        // 1) find by id : insertUniqueNameReq
        var findUsersByIdList1 = userService.findUsers(insertUniqueNameRsp.getUserId(), null);
        assertThat(findUsersByIdList1).isNotNull();
        assertThat(findUsersByIdList1.isEmpty()).isFalse();
        assertThat(findUsersByIdList1.size()).isOne();

        var findUsersById1 = findUsersByIdList1.get(0);
        assertThat(findUsersById1).isNotNull();
        assertThat(findUsersById1.getUserId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersById1.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersById1.isSolar()).isEqualTo(insertUniqueNameReq.isSolar());
        assertThat(findUsersById1.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.isBirthdayOpen());
        assertThat(findUsersById1.getBirthday()).isEqualTo(
                insertUniqueNameReq.isBirthdayOpen()? insertUniqueNameReq.getBirthday() : null
        );

        // 2) find by id : insertSameNameReq1
        var findUsersByIdList2 = userService.findUsers(insertSameNameRsp1.getUserId(), null);
        assertThat(findUsersByIdList2).isNotNull();
        assertThat(findUsersByIdList2.isEmpty()).isFalse();
        assertThat(findUsersByIdList2.size()).isOne();

        var findUsersById2 = findUsersByIdList2.get(0);
        assertThat(findUsersById2).isNotNull();
        assertThat(findUsersById2.getUserId()).isEqualTo(insertSameNameRsp1.getUserId());
        assertThat(findUsersById2.getName()).isEqualTo(insertSameNameReq1.getName());
        assertThat(findUsersById2.isSolar()).isEqualTo(insertSameNameReq1.isSolar());
        assertThat(findUsersById2.isBirthdayOpen()).isEqualTo(insertSameNameReq1.isBirthdayOpen());
        assertThat(findUsersById2.getBirthday()).isEqualTo(
                insertSameNameReq1.isBirthdayOpen()? insertSameNameReq1.getBirthday() : null
        );

        // 3) find by id : insertSameNameReq2
        var findUsersByIdList3 = userService.findUsers(insertSameNameRsp2.getUserId(), null);
        assertThat(findUsersByIdList3).isNotNull();
        assertThat(findUsersByIdList3.isEmpty()).isFalse();
        assertThat(findUsersByIdList3.size()).isOne();

        var findUsersById3 = findUsersByIdList3.get(0);
        assertThat(findUsersById3).isNotNull();
        assertThat(findUsersById3.getUserId()).isEqualTo(insertSameNameRsp2.getUserId());
        assertThat(findUsersById3.getName()).isEqualTo(insertSameNameReq2.getName());
        assertThat(findUsersById3.isSolar()).isEqualTo(insertSameNameReq2.isSolar());
        assertThat(findUsersById3.isBirthdayOpen()).isEqualTo(insertSameNameReq2.isBirthdayOpen());
        assertThat(findUsersById3.getBirthday()).isEqualTo(
                insertSameNameReq2.isBirthdayOpen()? insertSameNameReq2.getBirthday() : null
        );

        // 4) find by name : insertUniqueNameReq
        var findUsersByUniqueNameList = userService.findUsers(null, insertUniqueNameReq.getName());
        assertThat(findUsersByUniqueNameList).isNotNull();
        assertThat(findUsersByUniqueNameList.isEmpty()).isFalse();
        assertThat(findUsersByUniqueNameList.size()).isOne();

        var findUsersByUniqueName = findUsersByUniqueNameList.get(0);
        assertThat(findUsersByUniqueName).isNotNull();
        assertThat(findUsersByUniqueName.getUserId()).isEqualTo(insertUniqueNameRsp.getUserId());
        assertThat(findUsersByUniqueName.getName()).isEqualTo(insertUniqueNameReq.getName());
        assertThat(findUsersByUniqueName.isSolar()).isEqualTo(insertUniqueNameReq.isSolar());
        assertThat(findUsersByUniqueName.isBirthdayOpen()).isEqualTo(insertUniqueNameReq.isBirthdayOpen());
        assertThat(findUsersByUniqueName.getBirthday()).isEqualTo(
                insertUniqueNameReq.isBirthdayOpen()? insertUniqueNameReq.getBirthday() : null
        );

        // 5) find by name : insertSameNameReq1 or 2
        var findUsersBySameNameList = userService.findUsers(null, insertSameNameReq1.getName());
        assertThat(findUsersBySameNameList).isNotNull();
        assertThat(findUsersBySameNameList.isEmpty()).isFalse();
        assertThat(findUsersBySameNameList.size()).isEqualTo(2);
        
        for (var findUsersBySameName : findUsersBySameNameList) {
            var findUsersBySameNameReq = findUsersBySameName.getUserId() == insertSameNameRsp1.getUserId()?
                    insertSameNameReq1 : insertSameNameReq2;
            var findUsersBySameNameId = findUsersBySameName.getUserId() == insertSameNameRsp1.getUserId() ?
                    insertSameNameRsp1.getUserId() : insertSameNameRsp2.getUserId();

            assertThat(findUsersBySameName).isNotNull();
            assertThat(findUsersBySameName.getUserId()).isEqualTo(findUsersBySameNameId);
            assertThat(findUsersBySameName.getName()).isEqualTo(findUsersBySameNameReq.getName());
            assertThat(findUsersBySameName.isSolar()).isEqualTo(findUsersBySameNameReq.isSolar());
            assertThat(findUsersBySameName.isBirthdayOpen()).isEqualTo(findUsersBySameNameReq.isBirthdayOpen());
            assertThat(findUsersBySameName.getBirthday()).isEqualTo(
                    findUsersBySameNameReq.isBirthdayOpen()? findUsersBySameNameReq.getBirthday() : null
            );
        }
    }

    @Test
    @Order(3)
    @DisplayName("사용자 삭제")
    @Transactional
    void delete() {
        /* 0. Create Request */
        InsertUserDto.Request insertReq = new InsertUserDto.Request(
                1, "TESTS_SNS_ID", "before pushToken",
                "테스트 유저", "0720", true,
                false, DeviceOs.ANDROID
        );

        InsertUserDto.Response insRsp = userService.signUp(insertReq);
        assertThat(insRsp).isNotNull();
        assertThat(insRsp.getJoinDate()).isNotNull();
        assertThat(isNow(insRsp.getJoinDate())).isTrue();
        
        // TODO: 사용자에 방(참여방, 개인방, 방장), 일정(개인 일정, 참여방 일정, 개인방 일정) 생성 후 테스트
    }
    
    boolean isNow(String time) {
        return StringUtils.equals(LocalDateTime.now().format(format),
                LocalDateTime.parse(time, BaseTimeEntity.format).format(format));
    }
}
