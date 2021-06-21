package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PatchFriendStatusDto {

    @ApiModel(value = "PatchFriendStatus.Request", description = "nested class in PatchFriendStatusDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "사용자 번호", required = true)
        private final Long userId;

        @ApiModelProperty(value = "친구 번호", required = true)
        private final Long friendId;

        @ApiModelProperty(value = "상태", required = true)
        private final FriendStatus status;
    }

    @ApiModel(value = "PatchFriendStatus.Response", description = "nested class in PatchFriendStatusDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "친구 상태 수정 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-06-18 23:18:05")
        private final String patchDate;
    }
}
