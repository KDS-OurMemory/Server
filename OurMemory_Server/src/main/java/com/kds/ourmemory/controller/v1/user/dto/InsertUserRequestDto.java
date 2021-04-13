package com.kds.ourmemory.controller.v1.user.dto;

import static com.kds.ourmemory.util.DateUtil.currentTime;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.kds.ourmemory.entity.user.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InsertUserRequestDto {

    @Max(3)
    @Min(1)
    @ApiModelProperty(value="SNS 종류", required = true, example = "1: 카카오, 2:구글, 3: 네이버")
    private int snsType;
    
    @NotBlank
	@ApiModelProperty(value="SNS 로그인 번호", required = true)
	private String snsId;
	
    @NotBlank
	@ApiModelProperty(value="FCM 토큰", required = true)
	private String pushToken;
	
    @NotBlank
	@ApiModelProperty(value="사용자명(또는 닉네임)", required = true)
	private String name;
	
    @Nullable
    @Pattern(regexp = "^(0[1-9]|1[0-2])([0-2][0-9]|3[0-1])$")
	@ApiModelProperty(value="생일", required = false, example = "0717")
	private String birthday;
	
    @Nullable
	@ApiModelProperty(value="양력 여부", required = false)
	private boolean solar;
	
    @Nullable
	@ApiModelProperty(value="생일 공개여부", required = false)
	private boolean birthdayOpen;
	
	public User toEntity() {
	    return User.builder()
                .snsId(snsId)
                .snsType(snsType)
                .pushToken(pushToken)
                .push(true)
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
