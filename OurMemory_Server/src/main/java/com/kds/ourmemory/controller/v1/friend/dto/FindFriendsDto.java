package com.kds.ourmemory.controller.v1.friend.dto;

import com.kds.ourmemory.entity.user.User;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindFriendsDto {

    @ApiModel(value = "FindFriends.Response", description = "nested class in FindFriendsDto")
    @Getter
    public static class Response {
        private Long userId;
        private String name;

        public Response(User user) {
            userId = user.getId();
            name = user.getName();
        }
    }
}
