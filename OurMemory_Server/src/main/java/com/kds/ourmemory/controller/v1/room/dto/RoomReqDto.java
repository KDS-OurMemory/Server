package com.kds.ourmemory.controller.v1.room.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@ApiModel(value = "RoomReqDto", description = "Room API Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomReqDto {

    @ApiModelProperty(value = "방 이름")
    private String name;

    @ApiModelProperty(value = "사용자 번호", example = "50")
    private Long userId;

    @ApiModelProperty(value = "방 공개 여부")
    private boolean opened;

    @ApiModelProperty(value = "초대할 멤버", example = "[2,4]")
    private List<Long> member;

}
