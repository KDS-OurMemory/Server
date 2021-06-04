package com.kds.ourmemory.controller.v1.notice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteNoticeDto {

    @ApiModel(value = "DeleteNotice.Response", description = "nested class in DeleteNoticeDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "알림 삭제 날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private final String deleteDate;
    }
}
