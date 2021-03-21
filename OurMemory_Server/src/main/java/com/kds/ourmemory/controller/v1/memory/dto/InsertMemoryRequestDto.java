package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InsertMemoryRequestDto {
    @ApiModelProperty(value = "방 번호", required = true)
    private Long roomId;

    @ApiModelProperty(value = "일정 작성자 snsId", required = true)
    private String snsId;

    @ApiModelProperty(value = "일정 이름", required = true, example = "회의 일정")
    private String name;
    
    @ApiModelProperty(value = "일정 참여자", required = true, example = "[2,4,5]")
    private List<Long> members;

    @ApiModelProperty(value = "일정 내용", required = false)
    private String contents;

    @ApiModelProperty(value = "장소", required = false)
    private String place;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "시작 시간", required = true, example = "2021-03-15 22:11:00")
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "종료 시간", required = true, example = "2021-03-15 23:11:00")
    private Date endDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "첫 번째 알림 시간", required = false, example = "2021-03-14 22:00:00")
    private Date firstAlarm;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "두 번째 알림 시간", required = false, example = "2021-03-15 21:00:00")
    private Date secondAlarm;

    @ApiModelProperty(value = "배경색", required = true, example = "#FFFFFF")
    private String bgColor;

    @ApiModelProperty(value = "공유할 방 목록", required = false)
    private List<Long> roomIds;
}
