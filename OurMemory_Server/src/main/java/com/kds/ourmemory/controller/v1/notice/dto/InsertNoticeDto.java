package com.kds.ourmemory.controller.v1.notice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertNoticeDto {

    @ApiModel(value = "InsertNotice.Request", description = "nested class in InsertNoticeDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "사용자 번호", required = true)
        private Long userId;

        @ApiModelProperty(value = "알림 종류", required = true)
        private String type;

        @ApiModelProperty(value = "알림 문자열 값", required = true)
        private String value;
    }

    @ApiModel(value = "InsertNotice.Response", description = "nested class in InsertNoticeDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "알림 생성 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-0420 11:03")
        private final String createDate;
    }
}
