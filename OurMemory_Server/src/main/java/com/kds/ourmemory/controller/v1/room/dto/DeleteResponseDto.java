package com.kds.ourmemory.controller.v1.room.dto;

import lombok.Getter;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class DeleteResponseDto {
    @ApiModelProperty(value = "방 삭제 날짜", example = "20210317")
    private String deleteDate;
}
