package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemoryResponseDto {
    @ApiModelProperty(value="일정 추가한 날짜", example = "20210315")
    private String addDate;
}
