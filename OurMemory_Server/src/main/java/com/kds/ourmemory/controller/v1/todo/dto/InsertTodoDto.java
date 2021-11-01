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
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertTodoDto {

    @ApiModel(value = "InsertTodoDto.Request", description = "nested class in InsertTodoDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "작성자 번호", required = true, example = "50")
        private Long writer;

        @ApiModelProperty(value = "TODO 내용", required = true)
        private String contents;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @ApiModelProperty(value = "TODO 날짜", required = true, notes = "yyyy-MM-dd", example = "2021-10-26")
        private LocalDate todoDate;
    }

    @ApiModel(value = "InsertTodoDto.Response", description = "nested class in InsertTodoDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "TODO 번호", example = "5")
        private final Long todoId;

        @ApiModelProperty(value = "작성자 번호", example = "64")
        private final Long writerId;

        @ApiModelProperty(value = "TODO 내용", example = "회의 일정")
        private final String contents;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @ApiModelProperty(value = "TODO 날짜", notes = "yyyy-MM-dd")
        private final LocalDateTime todoDate;

        public Response(Todo todo) {
            this.todoId = todo.getId();
            this.writerId = todo.getWriter().getId();
            this.contents = todo.getContents();
            this.todoDate = todo.getTodoDate();
        }
    }
}
