package com.kds.ourmemory.controller.v1.room.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteRoomDto {

    @ApiModel(value = "DeleteRoomDto.Request", description = "nested class in DeleteRoomDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "사용자 번호", required = true, notes = "방을 삭제하려는 사용자 번호")
        private long userId;
    }

    @ApiModel(value = "DeleteRoomDto.Response", description = "nested class in DeleteRoomDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
    }
}
