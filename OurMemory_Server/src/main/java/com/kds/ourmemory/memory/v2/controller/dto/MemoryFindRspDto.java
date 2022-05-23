package com.kds.ourmemory.memory.v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.memory.v1.controller.dto.MemoryRspDto;
import com.kds.ourmemory.relation.v1.entity.AttendanceStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel(value = "MemoryFindRspDto", description = "Find Memory Response Dto")
@Getter
public class MemoryFindRspDto {

    @ApiModelProperty(value = "일정 번호", required = true, example = "5")
    private final Long memoryId;

    @ApiModelProperty(value = "일정 작성자 번호", required = true, example = "64")
    private final Long writerId;

    @ApiModelProperty(value = "일정 제목", required = true, example = "회의 일정")
    private final String name;

    @ApiModelProperty(value = "일정 내용", example = "주간 회의")
    private final String contents;

    @ApiModelProperty(value = "장소", example = "신도림역 1번 출구")
    private final String place;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "시작 시간(yyyy-MM-dd HH:mm)", required = true)
    private final LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "종료 시간(yyyy-MM-dd HH:mm)", required = true)
    private final LocalDateTime endDate;

    @ApiModelProperty(value = "배경색(16진 색상코드)", required = true, example = "#FFFFFF")
    private final String bgColor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "첫 번째 알림 시간(yyyy-MM-dd HH:mm)")
    private final LocalDateTime firstAlarm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "두 번째 알림 시간(yyyy-MM-dd HH:mm)")
    private final LocalDateTime secondAlarm;

    @ApiModelProperty(value = "일정 등록날짜(yyyy-MM-dd HH:mm:ss)", required = true)
    private final String regDate;

    @ApiModelProperty(value = "일정 수정날짜(yyyy-MM-dd HH:mm:ss)", required = true)
    private final String modDate;

    @ApiModelProperty(
            value = "참석 여부 목록(ABSENCE: 불참, ATTEND: 참석)",
            notes = "일정을 조회한 방 인원에 대한 참석 여부 목록을 전달한다. 참석 여부를 설정하지 않은 경우 미정으로 취급한다.",
            required = true
    )
    private final List<MemoryFindRspDto.UserAttendance> userAttendances;

    public MemoryFindRspDto(MemoryRspDto memoryRspDto) {
        memoryId = memoryRspDto.getMemoryId();
        writerId = memoryRspDto.getWriterId();
        name = memoryRspDto.getName();
        contents = memoryRspDto.getContents();
        place = memoryRspDto.getPlace();
        startDate = memoryRspDto.getStartDate();
        endDate = memoryRspDto.getEndDate();
        bgColor = memoryRspDto.getBgColor();
        firstAlarm = memoryRspDto.getFirstAlarm();
        secondAlarm = memoryRspDto.getSecondAlarm();
        regDate = memoryRspDto.getRegDate();
        modDate = memoryRspDto.getModDate();
        userAttendances = memoryRspDto.getUserAttendances().stream()
                .map(attendance -> new UserAttendance(attendance.getUserId(), attendance.getStatus()))
                .toList();
    }

    /**
     * Memory attendance non static inner class
     */
    @ApiModel(value = "MemoryFindRspDto.UserAttendance", description = "inner class in MemoryFindRspDto.Response")
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    private class UserAttendance {
        @ApiModelProperty(value = "사용자 번호", required = true)
        private final long userId;

        @ApiModelProperty(value = "참석 여부", required = true)
        private final AttendanceStatus status;
    }

}
