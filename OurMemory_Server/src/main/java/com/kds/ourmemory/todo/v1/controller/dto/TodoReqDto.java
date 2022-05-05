package com.kds.ourmemory.todo.v1.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.todo.v1.entity.Todo;
import com.kds.ourmemory.user.v1.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel(value = "TodoReqDto", description = "Todo API Request Dto")
@Getter
@Builder
@AllArgsConstructor
public class TodoReqDto {

    @ApiModelProperty(value = "작성자 번호", example = "64")
    private final Long writerId;

    @ApiModelProperty(value = "TODO 내용", example = "회의 일정")
    private final String contents;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "TODO 날짜(yyyy-MM-dd)", notes = "yyyy-MM-dd")
    private final LocalDate todoDate;

    public Todo toEntity(User writer) {
        return Todo.builder()
                .writer(writer)
                .contents(contents)
                .todoDate(todoDate.atStartOfDay())
                .build();
    }

}
