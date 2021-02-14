package com.kds.ourmemory.dto.signup;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponse {
	@ApiParam(value="resultCode", example = "0: success, 1: fail")
	private int result;
	
	@ApiParam("user jon Time")
	private String joinTime;
}
