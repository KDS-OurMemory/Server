package com.kds.ourmemory.controller.v1.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PatchTokenDto {

    @ApiModel(value = "PatchTokenDto.Request", description = "nested class in PatchTokenDto")
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Getter
    public static class Request {
        @ApiModelProperty(value = "변경할 FCM 푸시토큰 값", required = true)
        private String pushToken;
    }

}
