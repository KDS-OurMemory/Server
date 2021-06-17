package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddFriendDto {

    @ApiModel(value = "AddFriend.Request", description = "nested class in AddFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "사용자 번호")
        private final Long userId;

        @ApiModelProperty(value = "추가할 친구 번호")
        private final Long friendId;
    }

    @ApiModel(value = "AddFriendDto.Response", description = "nested class in AddFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value="친구 추가한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-05-13 14:33:05")
        private final String addDate;
    }
}
