package com.kds.ourmemory.controller.v1.user.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsertUserResponseDto {
    
    @JsonFormat(pattern = "yyyyMMdd")
    @ApiModelProperty(value="사용자 추가한 날짜", notes = "yyyyMMdd", example = "20210401")
	private Date joinDate;
}
