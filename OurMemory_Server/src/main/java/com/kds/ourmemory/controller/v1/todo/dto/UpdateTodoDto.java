package com.kds.ourmemory.controller.v1.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.todo.Todo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateTodoDto {

    @ApiModel(value = "UpdateTodoDto.Request", description = "nested class in UpdateTodoDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "TODO 내용")
        private String contents;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @ApiModelProperty(value = "TODO 날짜", notes = "yyyy-MM-dd")
        private LocalDate todoDate;
    }

    @ApiModel(value = "DeleteRoomDto.Response", description = "nested class in DeleteRoomDto")
    public static class Response {
        public Response(Todo todo) {
        }
    }
}
