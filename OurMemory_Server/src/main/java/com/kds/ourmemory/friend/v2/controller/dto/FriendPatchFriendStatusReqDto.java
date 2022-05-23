package com.kds.ourmemory.friend.v2.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendReqDto;
import com.kds.ourmemory.friend.v2.enums.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "FriendRequestReqDto", description = "RequestFriend Request Dto")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendPatchFriendStatusReqDto {

    @ApiModelProperty(value = "사용자 번호", required = true)
    private Long userId;

    @ApiModelProperty(value = "친구 사용자 번호", required = true)
    private Long friendUserId;

    @ApiModelProperty(value = "상태", required = true)
    private FriendStatus friendStatus;

    public FriendReqDto toDto() {
        return new FriendReqDto(userId, friendUserId, FriendStatus.toV1(friendStatus));
    }

}
