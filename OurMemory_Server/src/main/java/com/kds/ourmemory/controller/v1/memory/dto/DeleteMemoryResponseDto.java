package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteMemoryResponseDto {
    @JsonFormat(pattern = "yyyyMMdd")
    @ApiModelProperty(value = "일정 삭제 날짜", notes = "yyyyMMdd")
    private Date deleteDate;
}
