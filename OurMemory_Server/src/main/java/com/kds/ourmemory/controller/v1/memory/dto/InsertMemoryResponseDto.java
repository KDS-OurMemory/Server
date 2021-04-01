package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsertMemoryResponseDto {
    @ApiModelProperty(value = "일정 번호", example = "3")
    private Long memoryId;
    
    @ApiModelProperty(value = "방 번호", notes = "일정이 포함된 기준 방의 번호, 방에 포함되지 않는 경우 null 리턴됨.", example = "65")
    private Long roomId;
    
    @ApiModelProperty(value="일정 추가한 날짜", example = "20210315")
    private String addDate;
}
