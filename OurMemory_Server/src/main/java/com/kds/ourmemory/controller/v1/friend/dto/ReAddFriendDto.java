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
        @ApiModelProperty(value = "사용자 번호")
        private final Long userId;

        @ApiModelProperty(value = "재 추가할 친구 번호")
        private final Long friendUserId;
    }

}
