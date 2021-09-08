package com.kds.ourmemory.controller.v1.memory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
import com.kds.ourmemory.entity.relation.UserMemory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindMemoryDto {

    @ApiModel(value = "FindMemoryDto.Response", description = "nested class in FindMemoryDto")
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

        @ApiModelProperty(value = "참석 여부 목록", notes = "일정을 조회한 방 인원에 대한 참석 여부 목록을 전달한다. 참석 여부를 설정하지 않은 경우 미정으로 취급한다.")
        private final List<UserAttendance> userAttendances;

        public Response(Memory memory, List<UserMemory> userMemories) {
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
            this.userAttendances = userMemories.stream().map(UserAttendance::new).collect(toList());
        }

        /**
         * Memory attendance non static inner class
         */
        @ApiModel(value = "FindMemoryDto.Response.UserAttendance", description = "inner class in FindMemoryDto.Response")
        @Getter
        private class UserAttendance {
            @ApiModelProperty(value = "사용자 번호")
            private final long userId;

            @ApiModelProperty(value = "참석 여부", notes = "ATTEND: 참석, ABSENCE: 불참")
            private final AttendanceStatus status;

            protected UserAttendance(UserMemory userMemory) {
                this.userId = userMemory.getUser().getId();
                this.status = userMemory.getStatus();
            }
        }
    }
}
