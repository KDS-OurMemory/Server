package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class FindMemoryResponseDto {
    @ApiModelProperty(value = "일정 번호", example = "5")
    private Long id;
    
    @ApiModelProperty(value = "일정 작성자 번호", example = "64")
    private Long writer;
    
    @ApiModelProperty(value = "일정 제목", example = "회의 일정")
    private String name;
    
    @ApiModelProperty(value = "일정 내용", example = "주간 회의")
    private String contents;
    
    @ApiModelProperty(value = "장소", example = "신도림역 1번 출구")
    private String place;
    
    @ApiModelProperty(value = "시작 시간", notes = "yyyy-MM-dd HH:mm")
    private Date startDate;
    
    @ApiModelProperty(value = "종료 시간", notes = "yyyy-MM-dd HH:mm")
    private Date endDate;
    
    @ApiModelProperty(value = "배경색", example = "#FFFFFF")
    private String bgColor;
    
    @ApiModelProperty(value = "첫 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
    private Date firstAlarm;
    
    @ApiModelProperty(value = "두 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
    private Date secondAlarm;
    
    @ApiModelProperty(value = "일정 등록날짜", notes = "yyyyMMdd")
    private Date regDate;
    
    @ApiModelProperty(value = "일정 수정날짜", notes = "yyyyMMdd")
    private Date modDate;
    
    @ApiModelProperty(value = "일정 참여자", notes = "일정을 생성한 사람도 참여자에 포함되어 전달됨.", example = "[{참여자1}, {참여자2}]")
    private List<UserResponseDto> members = new ArrayList<>();
    
    public FindMemoryResponseDto(Memory memory) {
        id = memory.getId();
        writer = memory.getWriter().getId();
        name = memory.getName();
        contents = memory.getContents();
        place = memory.getPlace();
        startDate = memory.getStartDate();
        endDate = memory.getEndDate();
        bgColor = memory.getBgColor();
        firstAlarm = memory.getFirstAlarm();
        secondAlarm = memory.getSecondAlarm();
        regDate = memory.getRegDate();
        modDate = memory.getModDate();
        
        members = memory.getUsers().stream().filter(User::isUsed).map(UserResponseDto::new)
                .collect(Collectors.toList());
    }
}
