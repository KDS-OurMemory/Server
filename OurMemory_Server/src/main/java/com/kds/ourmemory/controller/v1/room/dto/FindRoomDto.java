package com.kds.ourmemory.controller.v1.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindRoomDto {
    
    @ApiModel(value = "FindRoomDto.Response", description = "nested class in FindRoomDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "방 번호", example = "5")
        private final long roomId;

        @ApiModelProperty(value = "방 소유자 번호", example = "17")
        private final long ownerId;

        @ApiModelProperty(value = "방 이름", example = "가족방")
        private final String name;

        @ApiModelProperty(value = "방 생성일", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-04-20 14:33:05")
        private final String regDate;

        @ApiModelProperty(value = "방 공개여부", example = "false")
        private final boolean opened;

        @ApiModelProperty(value = "방 참여자", example = "[{사용자}, {사용자2}]")
        private final List<Member> members;

        @ApiModelProperty(value = "방에 생성된 일정", example = "[{일정 제목, 시작시간, 종료시간}, ...]")
        private final List<FindRoomDto.Response.Memory> memories;

        public Response(Room room) {
            var now = LocalDateTime.now();

            roomId = room.getId();
            ownerId = room.getOwner().getId();
            name = room.getName();
            regDate = room.formatRegDate();
            opened = room.isOpened();
            members = room.getUsers().stream().filter(User::isUsed).map(FindRoomDto.Response.Member::new)
                    .collect(Collectors.toList());
            memories = room.getMemories().stream().filter(com.kds.ourmemory.entity.memory.Memory::isUsed)
                    .filter(memory -> memory.getEndDate().isAfter(now))
                    .map(FindRoomDto.Response.Memory::new)
                    .collect(Collectors.toList());
        }

        /**
         * Room Member non static inner class
         */
        @ApiModel(value = "FindRooms.Response.Member", description = "inner class in FindRoomDto.Response")
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

        /**
         * Room Memories non static inner class
         */
        @ApiModel(value = "FindRooms.Response.Memories", description = "inner class in FindRoomDto.Response")
        @Getter
        private class Memory {
            @ApiModelProperty(value = "일정 제목", example = "회의 일정")
            private final String name;

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
            @ApiModelProperty(value = "시작 시간", notes = "yyyy-MM-dd HH:mm")
            private final LocalDateTime startDate;

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
            @ApiModelProperty(value = "종료 시간", notes = "yyyy-MM-dd HH:mm")
            private final LocalDateTime endDate;

            protected Memory(com.kds.ourmemory.entity.memory.Memory memory) {
                this.name = memory.getName();
                this.startDate = memory.getStartDate();
                this.endDate = memory.getEndDate();
            }
        }
    }
}
