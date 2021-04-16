package com.kds.ourmemory.controller.v1.room.dto;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.util.DateUtil;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindRoomsDto {
    
    @Getter
    public static class Response {
        @ApiModelProperty(value = "방 번호", example = "5")
        private Long roomId;

        @ApiModelProperty(value = "방 소유자 번호", example = "17")
        private Long ownerId;

        @ApiModelProperty(value = "방 이름", example = "가족방")
        private String name;

        @JsonFormat(pattern = "yyyyMMdd")
        @ApiModelProperty(value = "방 생성일", notes = "yyyyMMdd", example = "20210316")
        private Date regDate;

        @ApiModelProperty(value = "방 공개여부", example = "false")
        private boolean opened;

        @ApiModelProperty(value = "방 참여자", example = "[{사용자}, {사용자2}]")
        private List<Member> members;

        public Response(Room room) {
            roomId = room.getId();
            ownerId = room.getOwner().getId();
            name = room.getName();
            regDate = DateUtil.formatDate(room.getRegDate());
            opened = room.isOpened();
            members = room.getUsers().stream().filter(User::isUsed).map(Member::new)
                    .collect(Collectors.toList());
        }
        
        /**
         * Room Member non static inner class
         */
        @Getter
        private class Member {
            @ApiModelProperty(value = "사용자 번호", example = "49")
            private Long userId;

            @ApiModelProperty(value = "사용자 이름", example = "김동영")
            private String name;

            @ApiModelProperty(value = "사용자 생일", example = "null")
            private String birthday;

            @ApiModelProperty(value = "양력 여부", example = "true")
            private boolean solar;

            @ApiModelProperty(value = "생일 공개여부", example = "false")
            private boolean birthdayOpen;
            
            public Member(User user) {
                userId = user.getId();
                name = user.getName();
                birthday = user.isBirthdayOpen() ? user.getBirthday() : null;
                solar = user.isSolar();
                birthdayOpen = user.isBirthdayOpen();
            }
        }
    }
}
