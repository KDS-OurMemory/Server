package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CancelFriendDto {

    @ApiModel(value = "CancelFriendDto.Request", description = "nested class in CancelFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "친구 요청한 사용자 번호")
        private final Long userId;

        @ApiModelProperty(value = "친구 요청 받은 사용자 번호")
        private final Long friendUserId;
    }

    @ApiModel(value = "CancelFriendDto.Response", description = "nested class in CancelFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "요청 취소 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-06-19 00:22:05")
        private final String cancelDate;
    }
}
