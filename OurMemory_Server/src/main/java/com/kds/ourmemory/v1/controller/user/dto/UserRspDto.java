package com.kds.ourmemory.v1.controller.user.dto;

import com.kds.ourmemory.v1.entity.user.DeviceOs;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.entity.user.UserRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "UserRspDto", description = "User API Response Dto")
@Getter
public class UserRspDto {
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

    public UserRspDto(User user) {
        this.userId = user.getId();
        this.name = user.getName();
        this.solar = user.isSolar();
        this.birthdayOpen = user.isBirthdayOpen();
        this.birthday = user.getBirthday();
        this.pushToken = user.getPushToken();
        this.push = user.isPush();
        this.snsType = user.getSnsType();
        this.snsId = user.getSnsId();
        this.privateRoomId = user.getPrivateRoomId();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole();
        this.deviceOs = user.getDeviceOs();
    }

}
