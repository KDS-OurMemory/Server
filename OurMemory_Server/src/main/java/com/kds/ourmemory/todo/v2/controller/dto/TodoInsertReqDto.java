package com.kds.ourmemory.todo.v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.todo.v1.controller.dto.TodoReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@ApiModel(value = "TodoInsertReqDto", description = "Insert Todo Request Dto")
@Getter
@Builder
@AllArgsConstructor
public class TodoInsertReqDto {

    @ApiModelProperty(value = "작성자 번호", example = "64", required = true)
    private final Long writerId;

    @ApiModelProperty(value = "TODO 내용", example = "회의 일정", required = true)
    private final String contents;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "TODO 날짜(yyyy-MM-dd)", notes = "yyyy-MM-dd", required = true)
    private final LocalDate todoDate;

    public TodoReqDto toDto() {
        return TodoReqDto.builder()
                .writerId(writerId)
                .contents(contents)
                .todoDate(todoDate)
                .build();
    }

}
