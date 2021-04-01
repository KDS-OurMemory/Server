package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.util.DateUtil;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class FindMemoryResponseDto {
    @ApiModelProperty(value = "일정 번호", example = "5")
    private Long memoryId;
    
    @ApiModelProperty(value = "일정 작성자 번호", example = "64")
    private Long writerId;
    
    @ApiModelProperty(value = "일정 제목", example = "회의 일정")
    private String name;
    
    @ApiModelProperty(value = "일정 내용", example = "주간 회의")
    private String contents;
    
    @ApiModelProperty(value = "장소", example = "신도림역 1번 출구")
    private String place;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "시작 시간", notes = "yyyy-MM-dd HH:mm")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "종료 시간", notes = "yyyy-MM-dd HH:mm")
    private Date endDate;
    
    @ApiModelProperty(value = "배경색", example = "#FFFFFF")
    private String bgColor;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "첫 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
    private Date firstAlarm;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "두 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
    private Date secondAlarm;
    
    @ApiModelProperty(value = "일정 등록날짜", notes = "yyyyMMdd")
    private String regDate;
    
    @ApiModelProperty(value = "일정 수정날짜", notes = "yyyyMMdd")
    private String modDate;
    
    @ApiModelProperty(value = "일정 참여자", notes = "일정을 생성한 사람도 참여자에 포함되어 전달됨.", example = "[{참여자1}, {참여자2}]")
    private List<UserResponseDto> members = new ArrayList<>();
    
    public FindMemoryResponseDto(Memory memory) {
        memoryId = memory.getId();
        writerId = memory.getWriter().getId();
        name = memory.getName();
        contents = memory.getContents();
        place = memory.getPlace();
        startDate = DateUtil.formatTime(memory.getStartDate());
        endDate = DateUtil.formatTime(memory.getEndDate());
        bgColor = memory.getBgColor();
        firstAlarm = DateUtil.formatTime(memory.getFirstAlarm());
        secondAlarm = DateUtil.formatTime(memory.getSecondAlarm());
        regDate = DateUtil.formatDate(memory.getRegDate());
        modDate = DateUtil.formatDate(memory.getModDate());
        
        members = memory.getUsers().stream().filter(User::isUsed).map(UserResponseDto::new)
                .collect(Collectors.toList());
    }
}
