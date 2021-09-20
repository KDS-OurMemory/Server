package com.kds.ourmemory.controller.v1.notice.dto;

import io.swagger.annotations.ApiModel;
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
    }
}
