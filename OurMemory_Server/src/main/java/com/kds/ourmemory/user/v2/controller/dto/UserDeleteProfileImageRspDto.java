package com.kds.ourmemory.user.v2.controller.dto;

import com.kds.ourmemory.user.v1.controller.dto.UserRspDto;
import com.kds.ourmemory.user.v1.entity.DeviceOs;
import com.kds.ourmemory.user.v1.entity.UserRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "UserDeleteProfileImageRspDto", description = "Delete ProfileImage Response Dto")
@Getter
public class UserDeleteProfileImageRspDto {

    @ApiModelProperty(value = "사용자 번호", required = true)
    private final Long userId;

    @ApiModelProperty(value = "사용자 이름", required = true, example = "김동영")
    private final String name;

    @ApiModelProperty(value = "사용자 생일(MMdd)", required = true, example = "0711")
    private final String birthday;

    @ApiModelProperty(value = "양력 여부", required = true, example = "true")
    private final boolean solar;

    @ApiModelProperty(value = "생일 공개여부", required = true, example = "false")
    private final boolean birthdayOpen;

    @ApiModelProperty(value = "FCM 푸시 토큰", required = true)
    private final String pushToken;

    @ApiModelProperty(value = "푸시 사용 여부", required = true)
    private final boolean push;

    @ApiModelProperty(value = "개인방 번호", required = true)
    private final long privateRoomId;

    @ApiModelProperty(value = "SNS 종류(1: 카카오, 2: 구글, 3: 네이버)", required = true)
    private final int snsType;

    @ApiModelProperty(value = "SNS ID", required = true)
    private final String snsId;

    @ApiModelProperty(value = "프로필사진 Url")
    private final String profileImageUrl;

    @ApiModelProperty(value = "역할(USER:사용자, ADMIN:관리자)", required = true, example = "USER")
    private final UserRole role;

    @ApiModelProperty(value = "사용기기 OS", required = true)
    private final DeviceOs deviceOs;

    public UserDeleteProfileImageRspDto(UserRspDto userRspDto) {
        this.userId = userRspDto.getUserId();
        this.name = userRspDto.getName();
        this.solar = userRspDto.isSolar();
        this.birthdayOpen = userRspDto.isBirthdayOpen();
        this.birthday = userRspDto.getBirthday();
        this.pushToken = userRspDto.getPushToken();
        this.push = userRspDto.isPush();
        this.snsType = userRspDto.getSnsType();
        this.snsId = userRspDto.getSnsId();
        this.privateRoomId = userRspDto.getPrivateRoomId();
        this.profileImageUrl = userRspDto.getProfileImageUrl();
        this.role = userRspDto.getRole();
        this.deviceOs = userRspDto.getDeviceOs();
    }

}
