package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InsertMemoryRequestDto {
    @ApiModelProperty(value = "일정 작성자 번호", required = true)
    private Long userId;
    
    @ApiModelProperty(value = "방 번호", required = false, notes = "일정을 등록할 방. 참여자가 있는데 방 번호가 없는 경우, 새로 생성한다.")
    private Long roomId;

    @ApiModelProperty(value = "일정 이름", required = true, example = "회의 일정")
    private String name;
    
    @ApiModelProperty(value = "일정 참여자 번호", required = false, notes = "참여자가 방에 포함된 사람과 다르거나 많을 경우, 방을 새로 생성한다.", example = "[2,4,5]")
    private List<Long> members;

    @ApiModelProperty(value = "일정 내용", required = false)
    private String contents;

    @ApiModelProperty(value = "장소", required = false)
    private String place;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "시작 시간", required = true, notes = "yyyy-MM-dd HH:mm", example = "2021-04-03 19:00")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "종료 시간", required = true, notes = "yyyy-MM-dd HH:mm", example = "2021-04-03 21:00")
    private Date endDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "첫 번째 알림 시간", required = false, notes = "yyyy-MM-dd HH:mm", example = "2021-04-01 12:00")
    private Date firstAlarm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "두 번째 알림 시간", required = false, notes = "yyyy-MM-dd HH:mm", example = "2021-04-02 12:00")
    private Date secondAlarm;

    @ApiModelProperty(value = "배경색", required = true, example = "#FFFFFF")
    private String bgColor;

    @ApiModelProperty(value = "일정을 공유할 방 목록", required = false, notes = "공유할 방의 허락을 받은 뒤 일정을 공유시킨다.")
    private List<Long> shareRooms;
}
