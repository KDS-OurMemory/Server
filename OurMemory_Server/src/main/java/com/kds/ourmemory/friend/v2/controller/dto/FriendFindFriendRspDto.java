package com.kds.ourmemory.friend.v2.controller.dto;

import com.kds.ourmemory.friend.v1.controller.dto.FriendRspDto;
import com.kds.ourmemory.friend.v1.entity.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "FriendFindFriendRspDto", description = "Find Friends Response Dto")
@Getter
public class FriendFindFriendRspDto {

    @ApiModelProperty(value = "친구 번호(친구 사용자 번호)", required = true, example = "99")
    private final Long friendId;

    @ApiModelProperty(value = "친구 이름", required = true, example = "김동영")
    private final String name;

    @ApiModelProperty(value = "친구 생일(MMdd)", required = true, example = "0724 | null")
    private final String birthday;

    @ApiModelProperty(value = "양력 여부", required = true, example = "true")
    private final boolean solar;

    @ApiModelProperty(value = "생일 공개여부", required = true, example = "false")
    private final boolean birthdayOpen;

    @ApiModelProperty(value = "친구 프로필사진 Url", required = true)
    private final String profileImageUrl;

    @ApiModelProperty(value = "친구 상태(요청 후 대기: WAIT, 요청받은 상태: REQUESTED_BY, 친구: FRIEND, 차단: BLOCK)")
    private final FriendStatus friendStatus;

    public FriendFindFriendRspDto(FriendRspDto friendRspDto) {
        friendId = friendRspDto.getFriendId();
        name = friendRspDto.getName();
        birthday = friendRspDto.getBirthday();
        solar = friendRspDto.isSolar();
        birthdayOpen = friendRspDto.isBirthdayOpen();
        profileImageUrl = friendRspDto.getProfileImageUrl();
        friendStatus = friendRspDto.getFriendStatus();
    }

}
