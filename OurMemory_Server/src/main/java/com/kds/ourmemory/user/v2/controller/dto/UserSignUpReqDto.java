package com.kds.ourmemory.user.v2.controller.dto;

import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@ApiModel(value = "UserSignUpReqDto", description = "Sign Up Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpReqDto {

    @ApiModelProperty(value="SNS 인증방식(1: 카카오, 2:구글, 3: 네이버)")
    private Integer snsType;

    @ApiModelProperty(value="SNS 로그인 Id")
    private String snsId;

    @ApiModelProperty(value="푸시 토큰")
    private String pushToken;

    @ApiModelProperty(value = "푸시 사용여부")
    private Boolean push;

    @ApiModelProperty(value="사용자명(또는 닉네임)")
    private String name;

    @ApiModelProperty(value="생일(MMdd)", example = "0717")
    private String birthday;

    @ApiModelProperty(value="양력 여부")
    private Boolean solar;

    @ApiModelProperty(value="생일 공개여부")
    private Boolean birthdayOpen;

    @ApiModelProperty(value="디바이스 OS")
    @Enumerated(EnumType.STRING)
    private DeviceOs deviceOs;

    @ApiModelProperty(value = "프로필 이미지 파일")
    private MultipartFile profileImage;

    public UserReqDto toDto() {
        return UserReqDto.builder()
                .snsId(snsId)
                .snsType(snsType)
                .pushToken(pushToken)
                .push(push)
                .name(name)
                .birthday(birthday)
                .solar(solar)
                .birthdayOpen(birthdayOpen)
                .deviceOs(deviceOs)
                .build();
    }

}
