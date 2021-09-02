package com.kds.ourmemory.controller.v1.memory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShareMemoryDto {

    @ApiModel(value = "ShareMemoryDto.Request", description = "nested class in ShareMemoryDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "공유 대상 종류", required = true)
        private ShareType type;

        @ApiModelProperty(value = "공유 대상 목록", required = true, notes = "공유 종류(ShareType) 에 맞춰 일정을 공유한다.")
        private List<Long> targetIds;
    }

    @ApiModel(value = "ShareMemoryDto.ShareType", description = "nested class in ShareMemoryDto")
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

    @ApiModel(value = "ShareMemoryDto.Response", description = "nested class in ShareMemoryDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "공유 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-07-06 23:58:33")
        private final String shareDate;
    }
}
