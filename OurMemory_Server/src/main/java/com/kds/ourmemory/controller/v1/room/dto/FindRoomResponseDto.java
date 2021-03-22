package com.kds.ourmemory.controller.v1.room.dto;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import com.kds.ourmemory.controller.v1.user.dto.SignInResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FindRoomResponseDto {
    @ApiModelProperty(value = "방 번호", example = "5")
    private Long id;

    @ApiModelProperty(value = "방 소유자", example = "17")
    private Long owner;

    @ApiModelProperty(value = "방 이름", example = "가족방")
    private String name;

    @ApiModelProperty(value = "방 생성일", example = "20210316")
    private String regTime;

    @ApiModelProperty(value = "방 공개여부", example = "false")
    private boolean opened;

    @ApiModelProperty(value = "방 참여자", example = "[{사용자}, {사용자2}]")
    private List<SignInResponseDto> member;

    public FindRoomResponseDto(Room room) {
        id = room.getId();
        owner = room.getOwner().getId();
        name = room.getName();
        regTime = new SimpleDateFormat("yyyyMMdd").format(room.getRegDate());
        opened = room.isOpened();
        member = room.getUsers().stream().filter(User::isUsed).map(SignInResponseDto::new)
                .collect(Collectors.toList());
    }
}
