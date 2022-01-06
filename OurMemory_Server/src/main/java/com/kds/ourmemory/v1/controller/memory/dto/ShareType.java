package com.kds.ourmemory.v1.controller.memory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel(value = "ShareType", description = "shareMemory API's shareType")
@Getter
@AllArgsConstructor
public enum ShareType {
    @ApiModelProperty(value = "1. 사용자 목록에 각각 공유", notes = "각 사용자와 방 생성 후 일정을 공유한다.")
    USERS("1. 사용자 목록에 각각 공유, 각 사용자와 방 생성 후 일정을 공유한다."),

    @ApiModelProperty(value = "2. 사용자 그룹에 각각 공유", notes = "사용자 그룹을 참여자로 방 생성 후 일정을 공유한다.")
    USER_GROUP("2. 사용자 그룹에 공유, 사용자 그룹을 참여자로 방 생성 후 일정을 공유한다."),

    @ApiModelProperty(value = "3. 방 목록에 각각 공유", notes = "존재하는 방에 일정만 공유하기 때문에 방 생성 X.")
    ROOMS("3. 방 목록에 각각 공유, 방 생성 X")
    ;
    private final String desc;
}