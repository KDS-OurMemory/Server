package com.kds.ourmemory.controller.v1.user.dto;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.DeviceOs;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.entity.user.UserRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.util.Objects;

@ApiModel(value = "UserDto", description = "사용자 API 응답 Dto")
@Getter
public class UserDto {
    @ApiModelProperty(value = "사용자 번호")
    private final Long userId;

    @ApiModelProperty(value = "사용자 이름", example = "김동영")
    private final String name;

    @ApiModelProperty(value = "사용자 생일", example = "null")
    private final String birthday;

    @ApiModelProperty(value = "양력 여부", example = "true")
    private final boolean solar;

    @ApiModelProperty(value = "생일 공개여부", example = "false")
    private final boolean birthdayOpen;

    @ApiModelProperty(value = "FCM 푸시 토큰")
    private final String pushToken;

    @ApiModelProperty(value = "푸시 사용 여부")
    private final boolean push;

    @ApiModelProperty(value = "개인방 번호")
    private final long privateRoomId;

    @ApiModelProperty(value = "SNS 종류(1: 카카오, 2: 구글, 3: 네이버)")
    private final int snsType;

    @ApiModelProperty(value = "SNS ID")
    private final String snsId;

    @ApiModelProperty(value = "프로필사진 Url")
    private final String profileImageUrl;

    @ApiModelProperty(value = "역할", example = "USER or ADMIN")
    private final UserRole role;

    @ApiModelProperty(value = "사용기기 OS", example = "ANDROID or IOS")
    private final DeviceOs deviceOs;

    @ApiModelProperty(value = "계정 사용여부")
    private final boolean used;

    /* used for findUsers API Response */
    @ApiModelProperty(value = "친구 상태(요청 후 대기: WAIT, 요청받은 상태: REQUESTED_BY, 친구: FRIEND, 차단: BLOCK | 관계없음: null)"
            , example = "FRIEND")
    private FriendStatus friendStatus;

    public UserDto(User user) {
        this.userId = user.getId();
        this.name = user.getName();
        this.solar = user.isSolar();
        this.birthdayOpen = user.isBirthdayOpen();
        this.birthday = user.getBirthday();
        this.pushToken = user.getPushToken();
        this.push = user.isPush();
        this.snsType = user.getSnsType();
        this.snsId = user.getSnsId();
        this.privateRoomId = user.getPrivateRoomId();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole();
        this.deviceOs = user.getDeviceOs();
        this.used = user.isUsed();
    }

    public UserDto(User user, Friend friend) {
        this.userId = user.getId();
        this.name = user.getName();
        this.solar = user.isSolar();
        this.birthdayOpen = user.isBirthdayOpen();
        this.birthday = user.getBirthday();
        this.pushToken = user.getPushToken();
        this.push = user.isPush();
        this.snsType = user.getSnsType();
        this.snsId = user.getSnsId();
        this.privateRoomId = user.getPrivateRoomId();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole();
        this.deviceOs = user.getDeviceOs();
        this.used = user.isUsed();

        this.friendStatus = Objects.nonNull(friend)? friend.getStatus() : null;
    }
}
