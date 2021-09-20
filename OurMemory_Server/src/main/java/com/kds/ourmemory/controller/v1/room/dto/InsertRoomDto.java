package com.kds.ourmemory.controller.v1.room.dto;

import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertRoomDto {

    @ApiModel(value = "InsertRoomDto.Request", description = "nested class in InsertRoomDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "방 이름", required = true)
        private String name;
        
        @ApiModelProperty(value = "방장 번호", required = true, example = "50")
        private Long owner;
        
        @ApiModelProperty(value = "방 공개 여부", required = true)
        private boolean opened;
        
        @ApiModelProperty(value = "초대할 멤버", example = "[2,4]")
        private List<Long> member;
    }

    @ApiModel(value = "InsertRoomDto.Response", description = "nested class in InsertRoomDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "방 번호", example = "5")
        private final long roomId;

        @ApiModelProperty(value = "방 소유자 번호", example = "17")
        private final long ownerId;

        @ApiModelProperty(value = "방 이름", example = "가족방")
        private final String name;

        @ApiModelProperty(value = "방 공개여부", example = "false")
        private final boolean opened;

        @ApiModelProperty(value = "방 참여자", example = "[{사용자}, {사용자2}]")
        private final List<Member> members;

        public Response(Room room) {
            roomId = room.getId();
            ownerId = room.getOwner().getId();
            name = room.getName();
            opened = room.isOpened();
            members = room.getUsers().stream().filter(User::isUsed).map(InsertRoomDto.Response.Member::new)
                    .collect(Collectors.toList());
        }

        /**
         * Room Member non static inner class
         */
        @ApiModel(value = "InsertRoomDto.Response.Member", description = "inner class in InsertRoomDto.Response")
        @Getter
        private class Member {
            @ApiModelProperty(value = "사용자 번호", example = "49")
            private final long userId;

            @ApiModelProperty(value = "사용자 이름", example = "김동영")
            private final String name;

            @ApiModelProperty(value = "사용자 생일", example = "null")
            private final String birthday;

            @ApiModelProperty(value = "양력 여부", example = "true")
            private final boolean solar;

            @ApiModelProperty(value = "생일 공개여부", example = "false")
            private final boolean birthdayOpen;

            protected Member(User user) {
                userId = user.getId();
                name = user.getName();
                birthday = user.isBirthdayOpen() ? user.getBirthday() : null;
                solar = user.isSolar();
                birthdayOpen = user.isBirthdayOpen();
            }
        }
    }
}
