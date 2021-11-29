package com.kds.ourmemory.controller.v1.memory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel(value = "MemoryReqDto", description = "Memory API Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemoryReqDto {

    @ApiModelProperty(value = "사용자 번호")
    private Long userId;

    @ApiModelProperty(value = "방 번호")
    private Long roomId;

    @ApiModelProperty(value = "일정 이름", example = "회의 일정")
    private String name;

    @ApiModelProperty(value = "일정 내용")
    private String contents;

    @ApiModelProperty(value = "장소")
    private String place;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "시작 시간(yyyy-MM-dd HH:mm)", example = "2021-04-03 19:00")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "종료 시간(yyyy-MM-dd HH:mm)", example = "2021-04-03 21:00")
    private LocalDateTime endDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "첫 번째 알림 시간(yyyy-MM-dd HH:mm)", example = "2021-04-01 12:00")
    private LocalDateTime firstAlarm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "두 번째 알림 시간(yyyy-MM-dd HH:mm)", example = "2021-04-02 12:00")
    private LocalDateTime secondAlarm;

    @ApiModelProperty(value = "배경색(16진 색상코드)", example = "#FFFFFF")
    private String bgColor;

    /* Used only setAttend */
    @ApiModelProperty(value = "[일정 참석여부용] 참석 여부", example = "ATTEND")
    private AttendanceStatus attendanceStatus;

    /* Used only shareMemory */
    @ApiModelProperty(value = "[일정 공유용] 대상 종류(USERS: 개별 사용자 목록, USER_GROUP: 사용자 그룹, ROOMS: 방 목록)")
    private ShareType shareType;

    @ApiModelProperty(value = "[일정 공유용] 대상 목록(공유 종류(ShareType) 에 맞춰 일정을 공유한다.)")
    private List<Long> shareIds;

    public Memory toEntity(User writer) {
        return Memory.builder()
                .writer(writer)
                .name(name)
                .contents(contents)
                .place(place)
                .startDate(startDate)
                .endDate(endDate)
                .firstAlarm(firstAlarm)
                .secondAlarm(secondAlarm)
                .bgColor(bgColor)
                .build();
    }

}
