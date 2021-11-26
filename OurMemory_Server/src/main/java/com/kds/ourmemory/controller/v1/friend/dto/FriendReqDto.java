package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel(value = "FriendReqDto", description = "Nested class in RequestFriendDto")
@Getter
@AllArgsConstructor
public class FriendReqDto {

    @ApiModelProperty(value = "사용자 번호", required = true)
    private final Long userId;

    @ApiModelProperty(value = "친구 사용자 번호", required = true)
    private final Long friendUserId;

    @ApiModelProperty(value = "상태")
    private FriendStatus status;

    public FriendReqDto(Long userId, Long friendUserId) {
        this.userId = userId;
        this.friendUserId = friendUserId;
    }

}
