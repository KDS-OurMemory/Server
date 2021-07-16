package com.kds.ourmemory.controller.v1.room.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PatchRoomOwnerDto {

    @ApiModel(value = "PatchRoomOwnerDto.Request", description = "nested class in PatchRoomOwnerDto")
    @Getter
    @AllArgsConstructor
    public static class Response {

        @ApiModelProperty(value = "수정 시간", notes = "yyyy-MM-dd HH:mm:ss")
        private final String patchDate;
    }
}
