package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.Objects;

@ApiModel(value = "FriendRspDto", description = "Friend API Response Dto")
@Getter
public class FriendRspDto {

    @ApiModelProperty(value = "친구 번호(친구 사용자 번호)", example = "99")
    private final Long friendId;

    @ApiModelProperty(value = "친구 이름", example = "김동영")
    private final String name;

    @ApiModelProperty(value = "친구 생일(MMdd)", example = "0724 | null")
    private final String birthday;

    @ApiModelProperty(value = "양력 여부", example = "true")
    private final boolean solar;

    @ApiModelProperty(value = "생일 공개여부", example = "false")
    private final boolean birthdayOpen;

    @ApiModelProperty(value = "친구 프로필사진 Url")
    private final String profileImageUrl;

    @ApiModelProperty(value = "친구 상태(요청 후 대기: WAIT, 요청받은 상태: REQUESTED_BY, 친구: FRIEND, 차단: BLOCK)")
    private final FriendStatus friendStatus;

    public FriendRspDto(Friend friend) {
        friendId = friend.getFriendUser().getId();
        name = friend.getFriendUser().getName();
        birthday = friend.getFriendUser().getBirthday();
        solar = friend.getFriendUser().isSolar();
        birthdayOpen = friend.getFriendUser().isBirthdayOpen();
        profileImageUrl = friend.getFriendUser().getProfileImageUrl();
        friendStatus = friend.getStatus();
    }

    // used for findUsers API Response
    public FriendRspDto(User user, Friend friend) {
        friendId = user.getId();
        name = user.getName();
        birthday = user.getBirthday();
        solar = user.isSolar();
        birthdayOpen = user.isBirthdayOpen();
        profileImageUrl = user.getProfileImageUrl();
        friendStatus = Objects.nonNull(friend) ? friend.getStatus() : null;
    }

}
