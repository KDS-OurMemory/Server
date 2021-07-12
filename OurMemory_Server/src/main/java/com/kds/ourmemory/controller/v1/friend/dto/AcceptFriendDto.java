package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AcceptFriendDto {

    @ApiModel(value = "InsertFriendDto.Request", description = "nested class in InsertFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "요청받은 사용자 번호")
        private final Long acceptUserId;

        @ApiModelProperty(value = "요청한 사용자 번호")
        private final Long requestUserId;
    }

    @ApiModel(value = "InsertFriendDto.Response", description = "nested class in InsertFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value="친구 요청 수락한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-05-13 14:33:05")
        private final String acceptDate;
    }
}
