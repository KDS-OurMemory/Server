package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReAddFriendDto {

    @ApiModel(value = "ReAddFriendDto.Request", description = "nested class in ReAddFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "친구 요청한 사용자 번호")
        private final Long fromUserId;

        @ApiModelProperty(value = "재 추가할 친구 번호")
        private final Long toUserId;
    }

    @ApiModel(value = "ReAddFriendDto.Response", description = "nested class in ReAddFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value="친구 재 추가한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-06-21 10:40:05")
        private final String reAddDate;
    }
}
