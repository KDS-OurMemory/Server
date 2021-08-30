package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AttendMemoryDto {

    @ApiModel(value = "SetMemoryAttendanceDto.Response", description = "nested class in SetMemoryAttendanceDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "참석 여부 설정한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-08-25 22:02:33")
        private final String setDate;
    }
}
