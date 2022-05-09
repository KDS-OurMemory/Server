package com.kds.ourmemory.friend.v2.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendRspDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

@ApiModel(value = "FriendCancelRspDto", description = "CancelRequest Response Dto")
@Getter
public class FriendCancelRequestRspDto {

    public FriendCancelRequestRspDto(FriendRspDto friendRspDto) {
    }

}
