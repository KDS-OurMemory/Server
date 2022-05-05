package com.kds.ourmemory.todo.v1.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.todo.v1.entity.Todo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel(value = "TodoRspDto", description = "Todo API Response Dto")
@Getter
public class TodoRspDto {

    @ApiModelProperty(value = "TODO 번호", required = true, example = "5")
    private final Long todoId;

    @ApiModelProperty(value = "작성자 번호", required = true, example = "64")
    private final Long writerId;

    @ApiModelProperty(value = "TODO 내용", required = true, example = "회의 일정")
    private final String contents;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "TODO 날짜(yyyy-MM-dd)", required = true)
    private final LocalDate todoDate;

    public TodoRspDto(Todo todo) {
        this.todoId = todo.getId();
        this.writerId = todo.getWriter().getId();
        this.contents = todo.getContents();
        this.todoDate = todo.getTodoDate().toLocalDate();
    }

}
