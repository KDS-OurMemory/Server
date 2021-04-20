package com.kds.ourmemory.controller.v1.memory.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindMemorysDto {
    
    @Getter
    public static class Response {
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
        private LocalDateTime startDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "종료 시간", notes = "yyyy-MM-dd HH:mm")
        private LocalDateTime endDate;
        
        @ApiModelProperty(value = "배경색", example = "#FFFFFF")
        private String bgColor;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "첫 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
        private LocalDateTime firstAlarm;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @ApiModelProperty(value = "두 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
        private LocalDateTime secondAlarm;
        
        @ApiModelProperty(value = "일정 등록날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private String regDate;
        
        @ApiModelProperty(value = "일정 수정날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private String modDate;
        
        @ApiModelProperty(value = "일정 참여자", notes = "일정을 생성한 사람도 참여자에 포함되어 전달됨.", example = "[{참여자1}, {참여자2}]")
        private List<Member> members = new ArrayList<>();
        
        public Response(Memory memory) {
            memoryId = memory.getId();
            writerId = memory.getWriter().getId();
            name = memory.getName();
            contents = memory.getContents();
            place = memory.getPlace();
            startDate = memory.getStartDate();
            endDate = memory.getEndDate();
            bgColor = memory.getBgColor();
            firstAlarm = memory.getFirstAlarm();
            secondAlarm = memory.getSecondAlarm();
            regDate = memory.formatRegDate();
            modDate = memory.formatModDate();
            
            members = memory.getUsers().stream().filter(User::isUsed).map(Member::new)
                    .collect(Collectors.toList());
        }
        
        /**
         * Memory member non static inner class
         */
        @Getter
        private class Member {
            @ApiModelProperty(value = "사용자 번호", example = "49")
            private Long userId;

            @ApiModelProperty(value = "사용자 이름", example = "김동영")
            private String name;

            @ApiModelProperty(value = "사용자 생일", example = "null")
            private String birthday;

            @ApiModelProperty(value = "양력 여부", example = "true")
            private boolean solar;

            @ApiModelProperty(value = "생일 공개여부", example = "false")
            private boolean birthdayOpen;
            
            public Member(User user) {
                userId = user.getId();
                name = user.getName();
                birthday = user.isBirthdayOpen() ? user.getBirthday() : null;
                solar = user.isSolar();
                birthdayOpen = user.isBirthdayOpen();
            }
        }
    }

}
