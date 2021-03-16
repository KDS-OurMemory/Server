package com.kds.ourmemory.controller.v1.memory.dto;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemoryRequestDto {
    @ApiModelProperty(value="일정 작성자 snsId", required = true)
    private String snsId;
    
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

    @Autowired
    private UserRepository userRepo;
    
    public MemoryRequestDto(String snsId, String name, String contents, String place, Date startDate, Date endDate, Date firstAlarm,
            Date secondAlarm, String bgColor) {
        this.snsId = snsId;
        this.name = name;
        this.contents = contents;
        this.place = place;
        this.startDate = startDate;
        this.endDate = endDate;
        this.firstAlarm = firstAlarm;
        this.secondAlarm = secondAlarm;
        this.bgColor = bgColor;
    }
     
    
    public Memory toEntity() {
        return Memory.builder()
                .id(userRepo.findBySnsId(snsId).map(User::getId).orElse(null))
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
