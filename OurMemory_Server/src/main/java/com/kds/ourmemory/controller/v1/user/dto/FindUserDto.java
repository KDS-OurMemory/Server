package com.kds.ourmemory.controller.v1.user.dto;

import com.kds.ourmemory.entity.user.User;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindUserDto {

    @ApiModel(value = "FindUser.Response", description = "nested class in FindUserDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "사용자 번호", example = "49")
        private long userId;

        @ApiModelProperty(value = "사용자 이름", example = "김동영")
        private String name;

        @ApiModelProperty(value = "사용자 생일", example = "null")
        private String birthday;

        @ApiModelProperty(value = "양력 여부", example = "true")
        private boolean solar;

        @ApiModelProperty(value = "생일 공개여부", example = "false")
        private boolean birthdayOpen;

        @ApiModelProperty(value = "FCM 푸시 토큰")
        private String pushToken;
        
        @ApiModelProperty(value = "푸시 사용여부")
        private boolean push;

        public Response(User user) {
            userId = user.getId();
            name = user.getName();
            birthday = user.isBirthdayOpen() ? user.getBirthday() : null;
            solar = user.isSolar();
            birthdayOpen = user.isBirthdayOpen();
            pushToken = user.getPushToken();
            push = user.isPush();
        }
    }
}
