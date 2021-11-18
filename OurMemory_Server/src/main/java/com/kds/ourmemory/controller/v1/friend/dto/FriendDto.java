package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "FriendDto", description = "Friend API Dto")
@Getter
@NoArgsConstructor
public class FriendDto {

    @ApiModelProperty(value = "친구 번호(친구 사용자 번호)", example = "99")
    private long friendId;

    @ApiModelProperty(value = "친구 이름", example = "김동영")
    private String name;

    @ApiModelProperty(value = "친구 생일", example = "0724 | null")
    private String birthday;

    @ApiModelProperty(value = "양력 여부", example = "true")
    private boolean solar;

    @ApiModelProperty(value = "생일 공개여부", example = "false")
    private boolean birthdayOpen;

    @ApiModelProperty(value = "친구 프로필사진 Url")
    private String profileImageUrl;

    @ApiModelProperty(value = "친구 상태(WAIT|REQUESTED_BY|FRIEND|BLOCK)")
    private FriendStatus friendStatus;

    public FriendDto(Friend friend) {
        friendId = friend.getFriendUser().getId();
        name = friend.getFriendUser().getName();
        birthday = friend.getFriendUser().getBirthday();
        solar = friend.getFriendUser().isSolar();
        birthdayOpen = friend.getFriendUser().isBirthdayOpen();
        profileImageUrl = friend.getFriendUser().getProfileImageUrl();
        friendStatus = friend.getStatus();
    }

}
