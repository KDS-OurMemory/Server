package com.kds.ourmemory.memory.v2.service;

import com.kds.ourmemory.room.v1.controller.dto.RoomReqDto;
import com.kds.ourmemory.room.v1.controller.dto.RoomRspDto;
import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import com.kds.ourmemory.user.v1.controller.dto.UserRspDto;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpReqDto;
import com.kds.ourmemory.user.v2.controller.dto.UserSignUpRspDto;
import com.kds.ourmemory.user.v2.service.UserV2Service;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoryV2ServiceTest {

    private final MemoryV2Service memoryV2Service;

    private final UserV2Service userV2Service;

    // Base data for test memoryService
    private UserSignUpRspDto insertWriterRsp;

    private UserSignUpRspDto insertMemberRsp;

//    private RoomInsertRspDto insertRoomRsp;

    @Autowired
    private MemoryV2ServiceTest(MemoryV2Service memoryV2Service, UserV2Service userV2Service) {
        this.memoryV2Service = memoryV2Service;
        this.userV2Service = userV2Service;
    }

    @Order(1)
    @Test
    void _1_일정추가_성공() {

    }
    // life cycle: @Before -> @Test => separate => Not maintained
    // Call function in @Test function => maintained
//    void setBaseData() {
//        /* 1. Create Writer, Member */
//        var insertWriterReq = UserSignUpReqDto.builder()
//                .snsType(2)
//                .snsId("writer_snsId")
//                .pushToken("member Token")
//                .push(true)
//                .name("member")
//                .birthday("0519")
//                .solar(true)
//                .birthdayOpen(false)
//                .deviceOs(DeviceOs.IOS)
//                .build();
//        insertWriterRsp = userV2Service.signUp(insertWriterReq);
//        assertThat(insertWriterRsp.getUserId()).isNotNull();
//
//        var insertMemberReq = UserReqDto.builder()
//                .snsType(1)
//                .snsId("member1_snsId")
//                .pushToken("member1 Token")
//                .push(true)
//                .name("member1")
//                .birthday("0720")
//                .solar(true)
//                .birthdayOpen(false)
//                .deviceOs(DeviceOs.AOS)
//                .build();
//        insertMemberRsp = userV2Service.signUp(insertMemberReq);
//        assertThat(insertMemberRsp.getUserId()).isNotNull();
//
//        /* 2. Create room */
//        var members = List.of(insertMemberRsp.getUserId());
//        var insertRoomReq = RoomReqDto.builder()
//                .name("room name")
//                .userId(insertWriterRsp.getUserId())
//                .opened(false)
//                .member(members)
//                .build();
//        insertRoomRsp = roomV2Service.insert(insertRoomReq);
//        assertThat(insertRoomRsp.getOwnerId()).isEqualTo(insertWriterRsp.getUserId());
//        assertThat(insertRoomRsp.getMembers().size()).isEqualTo(2);
//    }


}
