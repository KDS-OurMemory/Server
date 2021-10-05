package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindFriendsDto {

    @ApiModel(value = "FindFriendsDto.Response", description = "nested class in FindFriendsDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "친구 번호", example = "49")
        private final long friendId;

        @ApiModelProperty(value = "친구 이름", example = "김동영")
        private final String name;

        @ApiModelProperty(value = "친구 생일", example = "0724 | null")
        private final String birthday;

        @ApiModelProperty(value = "양력 여부", example = "true")
        private final boolean solar;

        @ApiModelProperty(value = "생일 공개여부", example = "false")
        private final boolean birthdayOpen;

        @ApiModelProperty(value = "친구 프로필사진 Url")
        private final String profileImageUrl;

        @ApiModelProperty(value = "친구 상태")
        private final FriendStatus status;

        public Response(Friend friend) {
            this.friendId = friend.getFriendUser().getId();
            this.name = friend.getFriendUser().getName();
            this.birthday = friend.getFriendUser().isBirthdayOpen()? friend.getFriendUser().getBirthday() : null;
            this.solar = friend.getFriendUser().isSolar();
            this.birthdayOpen = friend.getFriendUser().isBirthdayOpen();
            this.profileImageUrl = friend.getFriendUser().getProfileImageUrl();

            this.status = friend.getStatus();
        }
    }
}
