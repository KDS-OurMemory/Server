package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteMemoryResponseDto {
    @ApiModelProperty(value = "일정 삭제 날짜", example = "20210321")
    private String deleteDate;
}
