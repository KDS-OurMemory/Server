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

}
