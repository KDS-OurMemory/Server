package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "FriendReqDto", description = "Nested class in RequestFriendDto")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendReqDto {

    @ApiModelProperty(value = "사용자 번호", required = true)
    private Long userId;

    @ApiModelProperty(value = "친구 사용자 번호", required = true)
    private Long friendUserId;

    @ApiModelProperty(value = "상태")
    private FriendStatus status;

    public FriendReqDto(Long userId, Long friendUserId) {
        this.userId = userId;
        this.friendUserId = friendUserId;
    }

}
