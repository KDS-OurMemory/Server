package com.kds.ourmemory.controller.v1.user.dto;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindUsersDto {

    @ApiModel(value = "FindUsers.Response", description = "nested class in FindUsersDto")
    @Getter
    public static class Response {
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

        @ApiModelProperty(value = "친구 상태",
                example = "요청 후 대기: WAIT, 요청받은 상태: REQUESTED_BY, 친구: FRIEND, 차단: BLOCK | 관계없음: null")
        private final FriendStatus friendStatus;

       public Response(User user, Friend friend) {
            this.userId = user.getId();
            this.name = user.getName();
            this.birthday = user.isBirthdayOpen()? user.getBirthday() : null;
            this.solar = user.isSolar();
            this.birthdayOpen = user.isBirthdayOpen();
            this.friendStatus = Objects.nonNull(friend)? friend.getStatus() : null;
       }
    }
}
