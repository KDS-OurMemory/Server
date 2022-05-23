package com.kds.ourmemory.friend.v2.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendRspDto;
import com.kds.ourmemory.friend.v2.enums.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "FriendPatchFriendStatusRspDto", description = "Patch FriendStatus Response Dto")
@Getter
public class FriendPatchFriendStatusRspDto {

    @ApiModelProperty(value = "친구 번호(친구 사용자 번호)", required = true, example = "99")
    private final Long friendId;

    @ApiModelProperty(value = "친구 상태(요청 후 대기: WAIT, 요청받은 상태: REQUESTED_BY, 친구: FRIEND, 차단: BLOCK)")
    private final FriendStatus friendStatus;

    public FriendPatchFriendStatusRspDto(FriendRspDto friendRspDto) {
        friendId = friendRspDto.getFriendId();
        friendStatus = FriendStatus.toV2(friendRspDto.getFriendStatus());
    }

}
