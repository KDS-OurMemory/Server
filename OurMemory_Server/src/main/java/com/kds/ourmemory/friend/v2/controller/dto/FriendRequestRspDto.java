package com.kds.ourmemory.friend.v2.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendRspDto;
import com.kds.ourmemory.friend.v1.entity.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "FriendRequestRspDto", description = "RequestFriend Response Dto")
@Getter
public class FriendRequestRspDto {

    @ApiModelProperty(value = "친구 번호(친구 사용자 번호)", required = true, example = "99")
    private final Long friendId;

    @ApiModelProperty(value = "친구 상태(요청 후 대기: WAIT, 요청받은 상태: REQUESTED_BY, 친구: FRIEND, 차단: BLOCK)")
    private final FriendStatus friendStatus;

    public FriendRequestRspDto(FriendRspDto friendRspDto) {
        friendId = friendRspDto.getFriendId();
        friendStatus = friendRspDto.getFriendStatus();
    }

}