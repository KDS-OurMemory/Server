package com.kds.ourmemory.controller.v1.room.dto;

import com.kds.ourmemory.controller.v1.friend.dto.FriendRspDto;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.room.Room;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ApiModel(value = "RoomRspDto", description = "Room API Response Dto")
@Getter
public class RoomRspDto {

    @ApiModelProperty(value = "방 번호", example = "5")
    private final long roomId;

    @ApiModelProperty(value = "방 소유자 번호", example = "17")
    private final long ownerId;

    @ApiModelProperty(value = "방 이름", example = "가족방")
    private final String name;

    @ApiModelProperty(value = "방 생성일(yyyy-MM-dd HH:mm:ss)", example = "2021-04-20 14:33:05")
    private final String regDate;

    @ApiModelProperty(value = "방 공개여부", example = "false")
    private final boolean opened;

    @ApiModelProperty(value = "방 참여자")
    private final List<FriendRspDto> members;

    @ApiModelProperty(value = "방에 생성된 일정", example = "[{일정 제목, 시작시간, 종료시간}, ...]")
    private final List<MemoryDto> memories;

    @ApiModelProperty(value = "사용 여부", example = "true")
    private final boolean used;

    public RoomRspDto(Room room) {
        roomId = room.getId();
        ownerId = room.getOwner().getId();
        name = room.getName();
        regDate = room.formatRegDate();
        opened = room.isOpened();
        members = room.getUsers().stream().map(user -> new FriendRspDto(user, null)).collect(toList());
        memories = room.getMemories().stream().filter(Memory::isUsed)
                .filter(memory -> memory.getEndDate().isAfter(LocalDateTime.now()))
                .map(memory-> {
                    var userMemories = memory.getUsers()
                            .stream()
                            .filter(userMemory -> room.getUsers().contains(userMemory.getUser()))
                            .collect(toList());

                    return new MemoryDto(memory, userMemories);
                })
                .collect(toList());
        used = room.isUsed();
    }

}
