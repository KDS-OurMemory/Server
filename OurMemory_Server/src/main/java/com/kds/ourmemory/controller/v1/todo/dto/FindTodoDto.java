package com.kds.ourmemory.controller.v1.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.todo.Todo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindTodoDto {

    @ApiModel(value = "FindTodoDto.Response", description = "nested class in FindTodoDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "TODO 번호", example = "5")
        private final long todoId;

        @ApiModelProperty(value = "작성자 번호", example = "17")
        private final long writerId;

        @ApiModelProperty(value = "TODO 내용", example = "TIL 작성")
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
