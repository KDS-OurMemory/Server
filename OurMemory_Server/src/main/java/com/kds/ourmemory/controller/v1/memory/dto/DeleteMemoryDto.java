package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteMemoryDto {

    @ApiModel(value = "DeleteMemory.Response", description = "nested class in DeleteMemoryDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "일정 삭제 날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private String deleteDate;
    }
}
