package com.kds.ourmemory.dto.user;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {
	
	@ApiParam(value="sns ID", required = true)
	private String snsId;
	
	@ApiParam(value="sns Type", example = "1: 카카오, 2:구글, 3: 네이버", required = true)
	private int snsType;	// 1: 카카오, 2: 구글, 3: 네이버
	
	@ApiParam(value="FCM token", required = true)
	private String pushToken;
	
	@ApiParam(value="User Name or NickName", required = true)
	private String name;
	
	@ApiParam(value="birthday MM-dd", required = false)
	private String birthday;
	
	@ApiParam(value="is SolarCalendar", required = false)
	private boolean isSolar;
	
	@ApiParam(value="is Birthday Open other user", required = false)
	private boolean isBirthdayOpen;
}
