package com.kds.ourmemory.controller.v1.todo.dto;

import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteTodoDto {

    @ApiModel(value = "DeleteTodoDto.Response", description = "nested class in DeleteTodoDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
    }
}
