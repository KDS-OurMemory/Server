package com.kds.ourmemory.friend.v2.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "FriendReAddReqDto", description = "ReAdd Request Dto")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendReAddReqDto {

    @ApiModelProperty(value = "사용자 번호", required = true)
    private Long userId;

    @ApiModelProperty(value = "친구 사용자 번호", required = true)
    private Long friendUserId;

    public FriendReqDto toDto() {
        return new FriendReqDto(userId, friendUserId);
    }

}
