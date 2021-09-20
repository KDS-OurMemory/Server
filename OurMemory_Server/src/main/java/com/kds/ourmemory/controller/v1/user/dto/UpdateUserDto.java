package com.kds.ourmemory.controller.v1.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateUserDto {

    @ApiModel(value = "UpdateUserDto.Request", description = "nested class in UpdateUserDto")
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Getter
    public static class Request {

        @ApiModelProperty(value = "사용자명", notes = "null 또는 빈 값일 경우 업데이트 안함.", example = "다다다|null")
        private String name;

        @ApiModelProperty(value = "생일", notes = "MMdd, null 또는 빈 값일 경우 업데이트 안함.", example = "0101|null")
        private String birthday;

        @ApiModelProperty(value = "양력 여부", notes = "null 일 경우 업데이트 안함.", example = "true|false|null")
        private Boolean solar;

        @ApiModelProperty(value = "생일 공개여부", notes = "null 일 경우 업데이트 안함.", example = "true|false|null")
        private Boolean birthdayOpen;

        @ApiModelProperty(value = "푸시 사용여부", notes = "null 일 경우 업데이트 안함.", example = "true|false|null")
        private Boolean push;
    }

    @ApiModel(value = "UpdateUserDto.Response", description = "nested class in UpdateUserDto")
    @AllArgsConstructor
    @Getter
    public static class Response {
    }
}
