package com.kds.ourmemory.todo.v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.todo.v1.controller.dto.TodoRspDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel(value = "TodoInsertRspDto", description = "Insert Todo Response Dto")
@Getter
public class TodoInsertRspDto {

    @ApiModelProperty(value = "TODO 번호", required = true, example = "5")
    private final Long todoId;

    @ApiModelProperty(value = "작성자 번호", required = true, example = "64")
    private final Long writerId;

    @ApiModelProperty(value = "TODO 내용", required = true, example = "회의 일정")
    private final String contents;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "TODO 날짜(yyyy-MM-dd)", required = true)
    private final LocalDate todoDate;

    public TodoInsertRspDto(TodoRspDto todoRspDto) {
        this.todoId = todoRspDto.getTodoId();
        this.writerId = todoRspDto.getWriterId();
        this.contents = todoRspDto.getContents();
        this.todoDate = todoRspDto.getTodoDate();
    }

}
