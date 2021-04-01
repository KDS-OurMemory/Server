package com.kds.ourmemory.controller.v1.room.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsertRoomResponseDto {
    @ApiModelProperty(value = "방 번호", example = "3")
    private Long roomId;
    
    @JsonFormat(pattern = "yyyyMMdd")
    @ApiModelProperty(value="방 생성한 날짜", notes = "yyyyMMdd", example = "20210401")
    private Date createDate;
}
