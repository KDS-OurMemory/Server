package com.kds.ourmemory.controller.v1.user.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PatchTokenDto {

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Getter
    public static class Request {
        @ApiModelProperty(value = "변경할 FCM 푸시토큰 값", required = true)
        private String pushToken;
    }
    
    @AllArgsConstructor
    @Getter
    public static class Response {
        
        @JsonFormat(pattern = "yyyyMMdd")
        @ApiModelProperty(value = "업데이트 날짜", notes = "yyyyMMdd", example = "20210401")
        private Date patchDate;
    }
}
