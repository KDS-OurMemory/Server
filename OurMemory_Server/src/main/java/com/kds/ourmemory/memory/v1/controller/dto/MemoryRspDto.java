package com.kds.ourmemory.memory.v1.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.memory.v1.entity.Memory;
import com.kds.ourmemory.relation.v1.entity.AttendanceStatus;
import com.kds.ourmemory.relation.v1.entity.UserMemory;
import com.kds.ourmemory.room.v1.entity.Room;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@ApiModel(value = "MemoryRspDto", description = "Memory API Response Dto")
@Getter
public class MemoryRspDto {
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

    @ApiModelProperty(value = "일정 공유방 목록", notes = "일정이 공유된 방 목록")
    private List<MemoryRspDto.ShareRoom> shareRooms;

    @ApiModelProperty(value = "일정이 추가된 방", notes = "일정이 추가된 방 번호를 전달한다. 개인 일정인 경우 개인방 번호가 전달된다.")
    private Long addedRoomId;

    @ApiModelProperty(value = "참석 여부 목록(ABSENCE: 불참, ATTEND: 참석)", notes = "일정을 조회한 방 인원에 대한 참석 여부 목록을 전달한다. 참석 여부를 설정하지 않은 경우 미정으로 취급한다.")
    private List<UserAttendance> userAttendances;

    public MemoryRspDto(Memory memory) {
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
    }

    public MemoryRspDto(UserMemory userMemory) {
        memoryId = userMemory.getMemory().getId();
        writerId = userMemory.getMemory().getWriter().getId();
        name = userMemory.getMemory().getName();
        contents = userMemory.getMemory().getContents();
        place = userMemory.getMemory().getPlace();
        startDate = userMemory.getMemory().getStartDate();
        endDate = userMemory.getMemory().getEndDate();
        bgColor = userMemory.getMemory().getBgColor();
        firstAlarm = userMemory.getMemory().getFirstAlarm();
        secondAlarm = userMemory.getMemory().getSecondAlarm();
        regDate = userMemory.getMemory().formatRegDate();
        modDate = userMemory.getMemory().formatModDate();

        // Optional return
        userAttendances = List.of(new UserAttendance(userMemory));
    }

    public MemoryRspDto(Long privateRoomId, Memory memory) {
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

        // Optional return
        shareRooms = memory.getRooms().stream()
                .filter(room -> room.isUsed() && !room.getId().equals(privateRoomId))
                .map(MemoryRspDto.ShareRoom::new)
                .collect(Collectors.toList());
    }

    public MemoryRspDto(Memory memory, Long addedRoomId) {
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

        // Optional return
        this.addedRoomId = addedRoomId;
    }

    public MemoryRspDto(Memory memory, List<UserMemory> userMemories) {
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

        // Optional return
        this.userAttendances = userMemories.stream().map(MemoryRspDto.UserAttendance::new).collect(toList());
    }

    /**
     * Memory room non static inner class
     */
    @ApiModel(value = "MemoryDto.ShareRoom", description = "inner class in MemoryDto.Response")
    @Getter
    private class ShareRoom {
        @ApiModelProperty(value = "방 번호", required = true, example = "49")
        private final Long roomId;

        @ApiModelProperty(value = "방 소유자 번호", required = true, example = "99")
        private final Long ownerId;

        @ApiModelProperty(value = "방 이름", required = true, example = "프로젝트 방")
        private final String name;

        private ShareRoom(Room room) {
            roomId = room.getId();
            ownerId = room.getOwner().getId();
            name = room.getName();
        }
    }

    /**
     * Memory attendance non static inner class
     */
    @ApiModel(value = "MemoryDto.UserAttendance", description = "inner class in MemoryDto.Response")
    @Getter
    public class UserAttendance {
        @ApiModelProperty(value = "사용자 번호", required = true)
        private final long userId;

        @ApiModelProperty(value = "참석 여부", required = true)
        private final AttendanceStatus status;

        protected UserAttendance(UserMemory userMemory) {
            this.userId = userMemory.getUser().getId();
            this.status = userMemory.getStatus();
        }
    }
}
