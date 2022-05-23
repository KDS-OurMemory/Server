package com.kds.ourmemory.memory.v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.memory.v1.controller.dto.MemoryRspDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@ApiModel(value = "MemoryInsertRspDto", description = "Insert Memory Response Dto")
@Getter
public class MemoryInsertRspDto {

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
            value = "일정이 추가된 방",
            notes = "일정이 추가된 방 번호를 전달한다. 개인 일정인 경우 개인방 번호가 전달된다.",
            required = true
    )
    private final Long addedRoomId;

    public MemoryInsertRspDto(MemoryRspDto memoryRspDto) {
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
        addedRoomId = memoryRspDto.getAddedRoomId();
    }

}
