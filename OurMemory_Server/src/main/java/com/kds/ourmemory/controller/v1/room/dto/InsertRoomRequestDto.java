package com.kds.ourmemory.controller.v1.room.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InsertRoomRequestDto {
    @ApiModelProperty(value = "방 이름", required = true)
    private String name;
    
    @ApiModelProperty(value = "방장 번호", required = true, example = "50")
    private Long owner;
    
    @ApiModelProperty(value = "방 공개 여부", required = true)
    private boolean opened;
    
    @ApiModelProperty(value = "초대할 멤버", required = false, example = "[2,4]")
    private List<Long> member;
}
