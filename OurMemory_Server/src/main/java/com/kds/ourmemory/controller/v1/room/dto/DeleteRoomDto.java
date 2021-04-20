package com.kds.ourmemory.controller.v1.room.dto;

import com.kds.ourmemory.entity.BaseTimeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteRoomDto {

    @Getter
    @AllArgsConstructor
    public static class Response {
        
        @ApiModelProperty(value = "방 삭제 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-04-20 14:33:05")
        private BaseTimeEntity.CLocalDateTime deleteDate;
    }
}
