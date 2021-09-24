package com.kds.ourmemory.controller.v1.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileImageDto {

    @ApiModel(value = "FindUser.Response", description = "nested class in FindUserDto")
    @AllArgsConstructor
    @Getter
    public static class Response {
        @ApiModelProperty(value = "이미지 URL")
        private final String url;
    }
}
