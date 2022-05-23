package com.kds.ourmemory.room.v2.controller.dto;

import com.kds.ourmemory.friend.v2.controller.dto.FriendFindFriendsRspDto;
import com.kds.ourmemory.room.v1.controller.dto.RoomRspDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.List;

import static java.util.stream.Collectors.toList;

@ApiModel(value = "RoomInsertRspDto", description = "Insert Room Response Dto")
@Getter
public class RoomInsertRspDto {

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
    private final List<FriendFindFriendsRspDto> members;

    public RoomInsertRspDto(RoomRspDto roomRspDto) {
        roomId = roomRspDto.getRoomId();
        ownerId = roomRspDto.getOwnerId();
        name = roomRspDto.getName();
        regDate = roomRspDto.getRegDate();
        opened = roomRspDto.isOpened();
        members = roomRspDto.getMembers().stream().map(FriendFindFriendsRspDto::new).collect(toList());
    }

}
