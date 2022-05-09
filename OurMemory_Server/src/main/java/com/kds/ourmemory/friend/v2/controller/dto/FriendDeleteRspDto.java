package com.kds.ourmemory.friend.v2.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendRspDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

@ApiModel(value = "FriendDeleteRspDto", description = "Delete Friend Response Dto")
@Getter
public class FriendDeleteRspDto {

    public FriendDeleteRspDto(FriendRspDto friendRspDto) {
    }

}
