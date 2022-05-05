package com.kds.ourmemory.user.v2.controller.dto;

import com.kds.ourmemory.user.v1.controller.dto.UserRspDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

@ApiModel(value = "UserDeleteRspDto", description = "User Delete Response Dto")
@Getter
public class UserDeleteRspDto {

    public UserDeleteRspDto(UserRspDto userRspDto) {
    }

}
