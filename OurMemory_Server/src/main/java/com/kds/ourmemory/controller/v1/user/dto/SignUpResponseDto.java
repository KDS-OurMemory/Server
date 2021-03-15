package com.kds.ourmemory.controller.v1.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponseDto {
    @ApiModelProperty(value="사용자 추가한 날짜", example = "20210315")
	private String joinTime;
}
