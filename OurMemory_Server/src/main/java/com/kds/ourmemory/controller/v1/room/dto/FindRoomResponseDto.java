package com.kds.ourmemory.controller.v1.room.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.util.DateUtil;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class FindRoomResponseDto {
    @ApiModelProperty(value = "방 번호", example = "5")
    private Long roomId;

    @ApiModelProperty(value = "방 소유자 번호", example = "17")
    private Long ownerId;

    @ApiModelProperty(value = "방 이름", example = "가족방")
    private String name;

    @ApiModelProperty(value = "방 생성일", example = "20210316")
    private String regDate;

    @ApiModelProperty(value = "방 공개여부", example = "false")
    private boolean opened;

    @ApiModelProperty(value = "방 참여자", example = "[{사용자}, {사용자2}]")
    private List<UserResponseDto> members;

    public FindRoomResponseDto(Room room) {
        roomId = room.getId();
        ownerId = room.getOwner().getId();
        name = room.getName();
        regDate = DateUtil.formatDate(room.getRegDate());
        opened = room.isOpened();
        members = room.getUsers().stream().filter(User::isUsed).map(UserResponseDto::new)
                .collect(Collectors.toList());
    }
}
