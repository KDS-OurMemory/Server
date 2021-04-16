package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteMemoryDto {

    @Getter
    @AllArgsConstructor
    public static class Response {
        @JsonFormat(pattern = "yyyyMMdd")
        @ApiModelProperty(value = "일정 삭제 날짜", notes = "yyyyMMdd")
        private Date deleteDate;
    }
}
