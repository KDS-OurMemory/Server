package com.kds.ourmemory.controller.v1.user.dto;

import io.swagger.annotations.ApiModel;
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
    }
}
