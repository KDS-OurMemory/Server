package com.kds.ourmemory.controller.v1.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileImageDto {

    @ApiModel(value = "ProfileImageDto.Request", description = "nested class in PatchTokenDto")
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Request {
        @ApiModelProperty(value = "업로드할 프로필사진", required = true)
        private MultipartFile profileImage;
    }

    @ApiModel(value = "FindUser.Response", description = "nested class in FindUserDto")
    @AllArgsConstructor
    @Getter
    public static class Response {
        @ApiModelProperty(value = "이미지 URL")
        private final String url;
    }
}
