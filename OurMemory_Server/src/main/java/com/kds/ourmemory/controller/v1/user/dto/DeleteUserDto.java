package com.kds.ourmemory.controller.v1.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteUserDto {

    @ApiModel(value = "DeleteUserDto.Response", description = "nested class in DeleteUserDto")
    @Getter
    @AllArgsConstructor
    public static class Response {

        @ApiModelProperty(value = "사용자 삭제 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-07-13 23:07:05")
        private final String deleteDate;
    }
}
