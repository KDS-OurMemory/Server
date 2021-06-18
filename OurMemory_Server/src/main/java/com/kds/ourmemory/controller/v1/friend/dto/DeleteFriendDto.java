package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteFriendDto {

    @ApiModel(value = "DeleteFriend.Request", description = "nested class in DeleteFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "사용자 번호")
        private final Long userId;

        @ApiModelProperty(value = "삭제할 친구 번호")
        private final Long friendId;
    }

    @ApiModel(value = "DeleteFriend.Response", description = "nested class in DeleteFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value="친구 삭제한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-06-08 21:27:35")
        private final String deleteDate;
    }
}
