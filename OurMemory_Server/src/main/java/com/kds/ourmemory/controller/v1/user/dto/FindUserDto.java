package com.kds.ourmemory.controller.v1.user.dto;

import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindUserDto {

    @ApiModel(value = "FindUser.Response", description = "nested class in FindUserDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "사용자 번호", example = "99")
        private Long id;

        @ApiModelProperty(value = "sns 종류", example = "1: 카카오, 2: 구글, 3: 네이버")
        private int snsType;

        @ApiModelProperty(value = "sns id")
        private String snsId;

        @ApiModelProperty(value = "FCM 토큰 값")
        private String pushToken;

        @ApiModelProperty(value = "FCM 수신 여부")
        private boolean push;

        @ApiModelProperty(value = "이름")
        private String name;

        @ApiModelProperty(value = "생일", example = "MMdd")
        private String birthday;

        @ApiModelProperty(value = "양력 여부")
        private boolean solar;

        @ApiModelProperty(value = "생일 공개 여부")
        private boolean birthdayOpen;

        @ApiModelProperty(value = "역할", example = "user")
        @Enumerated(EnumType.STRING)
        private UserRole role;

        @ApiModelProperty(value = "계정 사용여부")
        private boolean used;

        @ApiModelProperty(value = "사용기기 OS", example = "Android, iOX")
        @Enumerated(EnumType.STRING)
        private DeviceOs deviceOs;

        public Response(User user ){
            this.id = user.getId();
            this.snsType = user.getSnsType();
            this.snsId = user.getSnsId();
            this.pushToken = user.getPushToken();
            this.push = user.isPush();
            this.name = user.getName();
            this.birthday = user.getBirthday();
            this.solar = user.isSolar();
            this.birthdayOpen = user.isBirthdayOpen();
            this.role = user.getRole();
            this.used = user.isUsed();
            this.deviceOs = user.getDeviceOs();
        }
    }
}
