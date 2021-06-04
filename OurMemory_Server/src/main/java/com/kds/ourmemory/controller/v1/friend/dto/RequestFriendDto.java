package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestFriendDto {

    @ApiModel(value = "RequestFriend.Request", description = "Nested class in RequestFriendDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "친구 요청할 사용자 번호")
        private Long friendId;
    }

    @ApiModel(value = "RequestFriend.Response", description = "Nested class in RequestFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value="친구 요청한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-05-24 22:19:05")
        private final String requestDate;
    }
}
