package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "FriendDto", description = "Friend API Dto")
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class FriendDto {

    @ApiModelProperty(value = "친구 번호(친구 사용자 번호)", example = "99")
    private Long friendId;

    @ApiModelProperty(value = "친구 이름", example = "김동영")
    private String name;

    @ApiModelProperty(value = "친구 생일(MMdd)", example = "0724 | null")
    private String birthday;

    @ApiModelProperty(value = "양력 여부", example = "true")
    private boolean solar;

    @ApiModelProperty(value = "생일 공개여부", example = "false")
    private boolean birthdayOpen;

    @ApiModelProperty(value = "친구 프로필사진 Url")
    private String profileImageUrl;

    @ApiModelProperty(value = "친구 상태(요청 후 대기: WAIT, 요청받은 상태: REQUESTED_BY, 친구: FRIEND, 차단: BLOCK)")
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

    // used for findUsers API Response
    public FriendDto(User user, Friend friend) {
        friendId = user.getId();
        name = user.getName();
        birthday = user.getBirthday();
        solar = user.isSolar();
        birthdayOpen = user.isBirthdayOpen();
        profileImageUrl = user.getProfileImageUrl();
        friendStatus = friend != null ? friend.getStatus() : null;
    }

}
