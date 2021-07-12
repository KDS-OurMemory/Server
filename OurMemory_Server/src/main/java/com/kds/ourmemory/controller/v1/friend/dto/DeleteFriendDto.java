package com.kds.ourmemory.controller.v1.friend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteFriendDto {

    @ApiModel(value = "DeleteFriendDto.Response", description = "nested class in DeleteFriendDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value="친구 삭제한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-06-08 21:27:35")
        private final String deleteDate;
    }
}
