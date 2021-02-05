package com.kds.ourmemory.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class SignUpResponse {
	private int result;
	private String joinTime;
}
