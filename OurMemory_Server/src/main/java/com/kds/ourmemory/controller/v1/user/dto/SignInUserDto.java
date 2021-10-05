package com.kds.ourmemory.controller.v1.user.dto;

import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignInUserDto {

    @ApiModel(value = "LoginUser.Response", description = "nested class in LoginUserDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "사용자 번호", example = "49")
        private final long userId;

        @ApiModelProperty(value = "사용자 이름", example = "김동영")
        private final String name;

        @ApiModelProperty(value = "사용자 생일", example = "null")
        private final String birthday;

        @ApiModelProperty(value = "양력 여부", example = "true")
        private final boolean solar;

        @ApiModelProperty(value = "생일 공개여부", example = "false")
        private final boolean birthdayOpen;

        @ApiModelProperty(value = "FCM 푸시 토큰")
        private final String pushToken;

        @ApiModelProperty(value = "푸시 사용 여부")
        private final boolean push;

        @ApiModelProperty(value = "개인방 번호")
        private final long privateRoomId;

        @ApiModelProperty(value = "프로필사진 Url")
        private final String profileImageUrl;

        public Response(User user) {
            userId = user.getId();
            name = user.getName();
            birthday = user.getBirthday();
            solar = user.isSolar();
            birthdayOpen = user.isBirthdayOpen();
            pushToken = user.getPushToken();
            push = user.isPush();
            privateRoomId = user.getPrivateRoomId();
            profileImageUrl = user.getProfileImageUrl();
        }
    }
}
