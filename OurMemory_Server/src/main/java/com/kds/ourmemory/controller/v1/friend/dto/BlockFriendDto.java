package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockFriendDto {

    @ApiModel(value = "BlockFriend.Request", description = "nested class in BlockFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "사용자 번호")
        private final Long userId;

        @ApiModelProperty(value = "차단할 친구 번호")
        private final Long friendId;
    }

    @ApiModel(value = "BlockFriend.Response", description = "nested class in BlockFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "친구 차단 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-06-18 23:18:05")
        private final String blockDate;
    }
}
