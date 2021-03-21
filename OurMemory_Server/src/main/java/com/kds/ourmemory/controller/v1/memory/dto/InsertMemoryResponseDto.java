package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsertMemoryResponseDto {
    @ApiModelProperty(value = "일정 번호", example = "3")
    private Long id;
    
    @ApiModelProperty(value="일정 추가한 날짜", example = "20210315")
    private String addDate;
}
