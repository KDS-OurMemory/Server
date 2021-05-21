package com.kds.ourmemory.controller.v1.user.dto;

import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindUsersDto {

    @ApiModel(value = "FindUsers.Response", description = "nested class in FindUsersDto")
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

        public Response(User user) {
            this.userId = user.getId();
            this.name = user.getName();
            this.birthday = user.isBirthdayOpen()? user.getBirthday() : null;
            this.solar = user.isSolar();
            this.birthdayOpen = user.isBirthdayOpen();
        }
    }
}
