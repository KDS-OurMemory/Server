package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindFriendsDto {

    @ApiModel(value = "FindFriends.Response", description = "nested class in FindFriendsDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "조회된 친구 번호")
        private final Long friendId;

        @ApiModelProperty(value = "조회된 친구 이름")
        private final String name;

        @ApiModelProperty(value = "친구 상태")
        private final FriendStatus status;

        public Response(Friend friend) {
            friendId = friend.getFriend().getId();
            name = friend.getFriend().getName();
            this.status = friend.getStatus();
        }
    }
}
