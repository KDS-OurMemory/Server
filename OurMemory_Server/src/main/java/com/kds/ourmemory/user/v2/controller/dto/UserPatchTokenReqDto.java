package com.kds.ourmemory.user.v2.controller.dto;

import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel(value = "UserPatchTokenReqDto", description = "Patch Token Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPatchTokenReqDto {

    @ApiModelProperty(value="푸시 토큰")
    private String pushToken;

    public UserReqDto toDto() {
        return UserReqDto.builder()
                .pushToken(pushToken)
                .build();
    }

}
