package com.kds.ourmemory.controller.v1.memory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.memory.Memory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertMemoryDto {

    @ApiModel(value = "InsertMemoryDto.Request", description = "nested class in InsertMemoryDto")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value = "일정 작성자 번호", required = true)
        private Long userId;
        
        @ApiModelProperty(value = "방 번호", notes = "일정을 등록할 방. 없는 경우 개인 일정으로 설정된다.")
        private Long roomId;

        @ApiModelProperty(value = "일정 이름", required = true, example = "회의 일정")
        private String name;
        
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
    }

    @ApiModel(value = "InsertMemoryDto.Response", description = "nested class in InsertMemoryDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "일정 번호", example = "5")
        private final Long memoryId;

        @ApiModelProperty(value = "일정 작성자 번호", example = "64")
        private final Long writerId;

        @ApiModelProperty(value = "일정 제목", example = "회의 일정")
        private final String name;

        @ApiModelProperty(value = "일정 내용", example = "주간 회의")
        private final String contents;

        @ApiModelProperty(value = "장소", example = "신도림역 1번 출구")
        private final String place;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "시작 시간", notes = "yyyy-MM-dd HH:mm")
        private final LocalDateTime startDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "종료 시간", notes = "yyyy-MM-dd HH:mm")
        private final LocalDateTime endDate;

        @ApiModelProperty(value = "배경색", example = "#FFFFFF")
        private final String bgColor;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "첫 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
        private final LocalDateTime firstAlarm;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "두 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
        private final LocalDateTime secondAlarm;

        @ApiModelProperty(value = "일정 등록날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private final String regDate;

        @ApiModelProperty(value = "일정 수정날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private final String modDate;

        @ApiModelProperty(value = "일정이 추가된 방", notes = "일정이 추가된 방 번호를 전달한다. 개인 일정인 경우 개인방 번호가 전달된다.")
        private final Long addedRoomId;

        public Response(Memory memory, Long addedRoomId) {
            memoryId = memory.getId();
            writerId = memory.getWriter().getId();
            name = memory.getName();
            contents = memory.getContents();
            place = memory.getPlace();
            startDate = memory.getStartDate();
            endDate = memory.getEndDate();
            bgColor = memory.getBgColor();
            firstAlarm = memory.getFirstAlarm();
            secondAlarm = memory.getSecondAlarm();
            regDate = memory.formatRegDate();
            modDate = memory.formatModDate();
            this.addedRoomId = addedRoomId;
        }
    }
}
