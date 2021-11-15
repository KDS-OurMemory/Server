package com.kds.ourmemory.controller.v1.memory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
import com.kds.ourmemory.entity.relation.UserMemory;
import com.kds.ourmemory.entity.room.Room;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@ApiModel(value = "MemoryDto", description = "Memory API Dto")
@Getter
public class MemoryDto {
    @ApiModelProperty(value = "일정 번호", example = "5")
    private final Long memoryId;

    @ApiModelProperty(value = "일정 작성자 번호", example = "64")
    private final Long writerId;

    @ApiModelProperty(value = "일정 제목", example = "회의 일정")
    private final String name;

    @ApiModelProperty(value = "일정 내용", example = "주간 회의")
    private final String contents;

    @ApiModelProperty(value = "장소", example = "신도림역 1번 출구")
    private final String place;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "시작 시간", notes = "yyyy-MM-dd HH:mm")
    private final LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "종료 시간", notes = "yyyy-MM-dd HH:mm")
    private final LocalDateTime endDate;

    @ApiModelProperty(value = "배경색", example = "#FFFFFF")
    private final String bgColor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "첫 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
    private final LocalDateTime firstAlarm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "두 번째 알림 시간", notes = "yyyy-MM-dd HH:mm")
    private final LocalDateTime secondAlarm;

    @ApiModelProperty(value = "일정 등록날짜", notes = "yyyy-MM-dd HH:mm:ss")
    private final String regDate;

    @ApiModelProperty(value = "일정 수정날짜", notes = "yyyy-MM-dd HH:mm:ss")
    private final String modDate;

    @ApiModelProperty(value = "일정 공유방 목록", notes = "일정이 공유된 방 목록")
    private List<MemoryDto.ShareRoom> shareRooms;

    @ApiModelProperty(value = "일정이 추가된 방", notes = "일정이 추가된 방 번호를 전달한다. 개인 일정인 경우 개인방 번호가 전달된다.")
    private Long addedRoomId;

    @ApiModelProperty(value = "참석 여부 목록", notes = "일정을 조회한 방 인원에 대한 참석 여부 목록을 전달한다. 참석 여부를 설정하지 않은 경우 미정으로 취급한다.")
    private List<UserAttendance> userAttendances;

    public MemoryDto(Memory memory) {
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

    public MemoryDto(UserMemory userMemory) {
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
        userAttendances = Stream.of(new UserAttendance(userMemory)).collect(toList());
    }

    public MemoryDto(Long privateRoomId, Memory memory) {
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
        shareRooms = memory.getRooms().stream()
                .filter(room -> room.isUsed() && !room.getId().equals(privateRoomId))
                .map(MemoryDto.ShareRoom::new)
                .collect(Collectors.toList());
    }

    public MemoryDto(Memory memory, Long addedRoomId) {
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

        this.addedRoomId = addedRoomId;
    }

    public MemoryDto(Memory memory, List<UserMemory> userMemories) {
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

        this.userAttendances = userMemories.stream().map(MemoryDto.UserAttendance::new).collect(toList());
    }

    /**
     * Memory room non static inner class
     */
    @ApiModel(value = "MemoryDto.ShareRoom", description = "inner class in MemoryDto.Response")
    @Getter
    private class ShareRoom {
        @ApiModelProperty(value = "방 번호", example = "49")
        private final Long roomId;

        @ApiModelProperty(value = "방 소유자 번호", example = "99")
        private final Long ownerId;

        @ApiModelProperty(value = "방 이름", example = "프로젝트 방")
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
    private class UserAttendance {
        @ApiModelProperty(value = "사용자 번호")
        private final long userId;

        @ApiModelProperty(value = "참석 여부", notes = "ATTEND: 참석, ABSENCE: 불참")
        private final AttendanceStatus status;

        protected UserAttendance(UserMemory userMemory) {
            this.userId = userMemory.getUser().getId();
            this.status = userMemory.getStatus();
        }
    }
}
