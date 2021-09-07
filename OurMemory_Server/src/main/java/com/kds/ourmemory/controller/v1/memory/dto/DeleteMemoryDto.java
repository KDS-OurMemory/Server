package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteMemoryDto {

    @ApiModel(value = "DeleteMemoryDto.Request", description = "nested class in DeleteMemoryDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "사용자 번호", required = true, notes = "일정을 삭제하려는 사용자 번호")
        private long userId;

        @ApiModelProperty(value = "일정을 삭제할 방 번호", required = true, notes = "일정을 삭제시킬 방 번호")
        private long targetRoomId;
    }

    @ApiModel(value = "DeleteMemoryDto.Response", description = "nested class in DeleteMemoryDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "일정 삭제 날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private final String deleteDate;
    }
}
