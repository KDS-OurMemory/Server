package com.kds.ourmemory.room.v1.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendRspDto;
import com.kds.ourmemory.memory.v1.controller.dto.MemoryRspDto;
import com.kds.ourmemory.friend.v1.entity.Friend;
import com.kds.ourmemory.memory.v1.entity.Memory;
import com.kds.ourmemory.room.v1.entity.Room;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ApiModel(value = "RoomRspDto", description = "Room API Response Dto")
@Getter
public class RoomRspDto {

    @ApiModelProperty(value = "방 번호", required = true, example = "5")
    private final long roomId;

    @ApiModelProperty(value = "방 소유자 번호", required = true, example = "17")
    private final long ownerId;

    @ApiModelProperty(value = "방 이름", example = "가족방")
    private final String name;

    @ApiModelProperty(value = "방 생성일(yyyy-MM-dd HH:mm:ss)", required = true)
    private final String regDate;

    @ApiModelProperty(value = "방 공개여부", required = true, example = "false")
    private final boolean opened;

    @ApiModelProperty(value = "방 참여자", required = true)
    private final List<FriendRspDto> members;

    @ApiModelProperty(value = "방에 생성된 일정", required = true)
    private final List<MemoryRspDto> memories;

    public RoomRspDto(Room room) {
        roomId = room.getId();
        ownerId = room.getOwner().getId();
        name = room.getName();
        regDate = room.formatRegDate();
        opened = room.isOpened();
        members = room.getUsers().stream().map(user -> new FriendRspDto(new Friend(user, user, null))).collect(toList());
        memories = room.getMemories().stream().filter(Memory::isUsed)
                .filter(memory -> memory.getEndDate().isAfter(LocalDateTime.now()))
                .map(memory-> {
                    var userMemories = memory.getUsers()
                            .stream()
                            .filter(userMemory -> room.getUsers().contains(userMemory.getUser()))
                            .collect(toList());

                    return new MemoryRspDto(memory, userMemories);
                })
                .collect(toList());
    }

}
