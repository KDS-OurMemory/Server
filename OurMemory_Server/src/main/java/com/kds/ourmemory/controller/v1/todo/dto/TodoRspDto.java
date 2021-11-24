package com.kds.ourmemory.controller.v1.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.todo.Todo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel(value = "TodoRspDto", description = "Todo API Response Dto")
@Getter
public class TodoRspDto {

    @ApiModelProperty(value = "TODO 번호", example = "5")
    private final Long todoId;

    @ApiModelProperty(value = "작성자 번호", example = "64")
    private final Long writerId;

    @ApiModelProperty(value = "TODO 내용", example = "회의 일정")
    private final String contents;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "TODO 날짜(yyyy-MM-dd)", notes = "yyyy-MM-dd")
    private final LocalDate todoDate;

    @ApiModelProperty(value = "사용 여부", example = "true")
    private final boolean used;

    public TodoRspDto(Todo todo) {
        this.todoId = todo.getId();
        this.writerId = todo.getWriter().getId();
        this.contents = todo.getContents();
        this.todoDate = todo.getTodoDate().toLocalDate();
        this.used = todo.isUsed();
    }

}
