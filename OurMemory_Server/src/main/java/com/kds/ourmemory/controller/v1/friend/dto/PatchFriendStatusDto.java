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

    @ApiModel(value = "PatchFriendStatusDto.Request", description = "nested class in PatchFriendStatusDto")
    @Getter
    @AllArgsConstructor
    public static class Request {
        @ApiModelProperty(value = "사용자 번호", required = true)
        private final Long userId;

        @ApiModelProperty(value = "친구 사용자 번호", required = true)
        private final Long friendUserId;

        @ApiModelProperty(value = "상태", required = true)
        private final FriendStatus status;
    }

    @ApiModel(value = "PatchFriendStatusDto.Response", description = "nested class in PatchFriendStatusDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
    }
}
