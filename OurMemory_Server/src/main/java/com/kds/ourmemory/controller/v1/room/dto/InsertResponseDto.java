package com.kds.ourmemory.controller.v1.room.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsertResponseDto {
    @ApiModelProperty(value="방 생성한 날짜", example = "20210315")
    private String createTime;
}
