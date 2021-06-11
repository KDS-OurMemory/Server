package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindFriendsDto {

    @ApiModel(value = "FindFriends.Response", description = "nested class in FindFriendsDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "조회된 사용자 번호")
        private final Long userId;

        @ApiModelProperty(value = "조회된 사용자 이름")
        private final String name;

        public Response(User user) {
            userId = user.getId();
            name = user.getName();
        }
    }
}
