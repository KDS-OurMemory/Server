package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertFriendDto {

    @ApiModel(value = "InsertFriend.Request", description = "nested class in InsertFriendDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        private Long friendId;
    }

    @ApiModel(value = "InsertFriend.Response", description = "nested class in InsertFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value="친구 추가한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-05-13 14:33:05")
        private final String addDate;
    }
}
