package com.kds.ourmemory.controller.v1.room.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertRoomDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "방 이름", required = true)
        private String name;
        
        @ApiModelProperty(value = "방장 번호", required = true, example = "50")
        private Long owner;
        
        @ApiModelProperty(value = "방 공개 여부", required = true)
        private boolean opened;
        
        @ApiModelProperty(value = "초대할 멤버", required = false, example = "[2,4]")
        private List<Long> member;
    }
    
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "방 번호", example = "3")
        private long roomId;
        
        @ApiModelProperty(value="방 생성한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-04-20 14:33:05")
        private String createDate;
    }
}
