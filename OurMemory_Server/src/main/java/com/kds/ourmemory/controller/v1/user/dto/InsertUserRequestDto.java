package com.kds.ourmemory.controller.v1.user.dto;

import static com.kds.ourmemory.util.DateUtil.currentTime;

import com.kds.ourmemory.entity.user.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InsertUserRequestDto {
	
	@ApiModelProperty(value="SNS 로그인 번호", required = true)
	private String snsId;
	
	@ApiModelProperty(value="SNS 종류", required = true, example = "1: 카카오, 2:구글, 3: 네이버")
	private int snsType;
	
	@ApiModelProperty(value="FCM 토큰", required = true)
	private String pushToken;
	
	@ApiModelProperty(value="사용자명(또는 닉네임)", required = true)
	private String name;
	
	@ApiModelProperty(value="생일", required = false, example = "0717")
	private String birthday;
	
	@ApiModelProperty(value="양력 여부", required = false)
	private boolean solar;
	
	@ApiModelProperty(value="생일 공개여부", required = false)
	private boolean birthdayOpen;
	
	public User toEntity() {
	    return User.builder()
                .snsId(snsId)
                .snsType(snsType)
                .pushToken(pushToken)
                .name(name)
                .birthday(birthday)
                .solar(solar)
                .birthdayOpen(birthdayOpen)
                .role("user")
                .regDate(currentTime())
                .used(true)
                .build();
	}
}
