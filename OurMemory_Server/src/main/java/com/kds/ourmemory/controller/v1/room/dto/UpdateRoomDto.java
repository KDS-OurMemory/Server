package com.kds.ourmemory.controller.v1.room.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateRoomDto {

    @ApiModel(value = "UpdateRoomDto.Request", description = "nested class in UpdateRoomDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "방 이름")
        private String name;

        @ApiModelProperty(value = "방 공개 여부")
        private Boolean opened;
    }

    @ApiModel(value = "UpdateRoomDto.Response", description = "nested class in UpdateRoomDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "수정 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-07-05 22:03:33")
        private final String updateDate;
    }
}
