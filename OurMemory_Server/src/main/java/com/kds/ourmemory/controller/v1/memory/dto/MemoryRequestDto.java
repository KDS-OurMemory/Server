package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.Date;

import com.kds.ourmemory.entity.memory.Memorys;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemoryRequestDto {
    private String name;
    private String contents;
    private String place;
    private Date startDate;
    private Date endDate;
    private Date firstAlarm;
    private Date secondAlarm;
    private String bgColor;
    
    public Memorys toEntity() {
        return Memorys.builder()
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