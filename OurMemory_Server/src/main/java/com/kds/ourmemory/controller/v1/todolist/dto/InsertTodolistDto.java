package com.kds.ourmemory.controller.v1.todolist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.todolist.Todolist;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertTodolistDto {

    @ApiModel(value = "InsertTodolistDto.Request", description = "nested class in InsertTodolistDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "작성자 번호", required = true, example = "50")
        private Long writer;

        @ApiModelProperty(value = "TODO 내용", required = true)
        private String contents;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "TODO 날짜", required = true, notes = "yyyy-MM-dd", example = "2021-10-26")
        private LocalDateTime todoDate;
    }

    @ApiModel(value = "InsertTodolistDto.Response", description = "nested class in InsertTodolistDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "TODO 리스트 번호", example = "5")
        private final Long todolistId;

        @ApiModelProperty(value = "작성자 번호", example = "64")
        private final Long writerId;

        @ApiModelProperty(value = "TODO 내용", example = "회의 일정")
        private final String contents;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @ApiModelProperty(value = "TODO 날짜", notes = "yyyy-MM-dd")
        private final LocalDateTime todoDate;

        public Response(Todolist todolist) {
            this.todolistId = todolist.getId();
            this.writerId = todolist.getWriter().getId();
            this.contents = todolist.getContents();
            this.todoDate = todolist.getTodoDate();
        }
    }
}
