package com.kds.ourmemory.controller.v1.user.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PatchUserTokenResponseDto {
    
    @JsonFormat(pattern = "yyyyMMdd")
    @ApiModelProperty(value = "업데이트 날짜", notes = "yyyyMMdd", example = "20210401")
    private Date patchDate;
}
