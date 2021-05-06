package com.kds.ourmemory.controller.v1.memory.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertMemoryDto {

    @ApiModel(value = "InsertMemory.Request", description = "nested class in InsertMemoryDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "일정 작성자 번호", required = true)
        private Long userId;
        
        @ApiModelProperty(value = "방 번호", notes = "일정을 등록할 방. 참여자가 있는데 방 번호가 없는 경우, 새로 생성한다.")
        private Long roomId;

        @ApiModelProperty(value = "일정 이름", required = true, example = "회의 일정")
        private String name;
        
        @ApiModelProperty(value = "일정 참여자 번호", notes = "참여자가 방에 포함된 사람과 다르거나 많을 경우, 방을 새로 생성한다.", example = "[2,4,5]")
        private List<Long> members;

        @ApiModelProperty(value = "일정 내용")
        private String contents;

        @ApiModelProperty(value = "장소")
        private String place;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "시작 시간", required = true, notes = "yyyy-MM-dd HH:mm", example = "2021-04-03 19:00")
        private LocalDateTime startDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "종료 시간", required = true, notes = "yyyy-MM-dd HH:mm", example = "2021-04-03 21:00")
        private LocalDateTime endDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "첫 번째 알림 시간", notes = "yyyy-MM-dd HH:mm", example = "2021-04-01 12:00")
        private LocalDateTime firstAlarm;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "두 번째 알림 시간", notes = "yyyy-MM-dd HH:mm", example = "2021-04-02 12:00")
        private LocalDateTime secondAlarm;

        @ApiModelProperty(value = "배경색", required = true, example = "#FFFFFF")
        private String bgColor;

        @ApiModelProperty(value = "일정을 공유할 방 목록", notes = "공유할 방의 허락을 받은 뒤 일정을 공유시킨다.")
        private List<Long> shareRooms;
    }

    @ApiModel(value = "InsertMemory.Response", description = "nested class in InsertMemoryDto")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "일정 번호", example = "3")
        private final long memoryId;
        
        @ApiModelProperty(value = "방 번호", notes = "일정이 포함된 기준 방의 번호, 방에 포함되지 않는 경우 null 리턴됨.", example = "65")
        private final Long roomId;
        
        @ApiModelProperty(value="일정 추가한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-04-20 14:49:33")
        private final String addDate;
    }
}