package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.Date;

import com.kds.ourmemory.entity.memory.Memory;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemoryRequestDto {
    @ApiModelProperty(value="일정 작성자 번호", required = true, example = "49")
    private Long writer;
    
    @ApiModelProperty(value="일정 이름", required = true, example = "회의 일정")
    private String name;
    
    @ApiModelProperty(value="일정 내용", required = false)
    private String contents;
    
    @ApiModelProperty(value="장소", required = false)
    private String place;
    
    @ApiModelProperty(value="시작 시간", required = true, example = "2021-03-15 22:11:00")
    private Date startDate;
    
    @ApiModelProperty(value="종료 시간", required = true, example = "2021-03-15 23:11:00")
    private Date endDate;
    
    @ApiModelProperty(value="첫 번째 알림 시간", required = false, example = "2021-03-14 22:00:00")
    private Date firstAlarm;
    
    @ApiModelProperty(value="두 번째 알림 시간", required = false, example = "2021-03-15 21:00:00")
    private Date secondAlarm;
    
    @ApiModelProperty(value="배경색", required = true, example = "#FFFFFF")
    private String bgColor;
    
    public Memory toEntity() {
        return Memory.builder()
                .writer(writer)
                .name(this.name)
                .contents(contents)
                .place(place)
                .startDate(startDate)
                .endDate(endDate)
                .bgColor(bgColor)
                .firstAlarm(firstAlarm)
                .secondAlarm(secondAlarm)
                .regDate(new Date())
                .modDate(null)
                .used(true)
                .build();
    }
}
