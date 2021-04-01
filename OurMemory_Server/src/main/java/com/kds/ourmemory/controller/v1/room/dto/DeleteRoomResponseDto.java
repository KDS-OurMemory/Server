package com.kds.ourmemory.controller.v1.room.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteRoomResponseDto {
    
    @JsonFormat(pattern = "yyyyMMdd")
    @ApiModelProperty(value = "방 삭제 날짜", notes = "yyyyMMdd", example = "20210401")
    private Date deleteDate;
}
