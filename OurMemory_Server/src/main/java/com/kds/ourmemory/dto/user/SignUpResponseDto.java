package com.kds.ourmemory.dto.user;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponseDto {
	@ApiParam(value="resultCode", example = "0: success, 1: fail")
	private int result;
	
	@ApiParam("user jon Time")
	private String joinTime;
}
