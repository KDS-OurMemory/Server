package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModel;
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
    }
}
