package com.kds.ourmemory.user.v2.controller.dto;

import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel(value = "UserUpdateReqDto", description = "Update User Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUpdateReqDto {

    @ApiModelProperty(value = "푸시 사용여부")
    private Boolean push;

    @ApiModelProperty(value="사용자명(또는 닉네임)")
    private String name;

    @ApiModelProperty(value="생일(MMdd)", example = "0717")
    private String birthday;

    @ApiModelProperty(value="양력 여부")
    private Boolean solar;

    @ApiModelProperty(value="생일 공개여부")
    private Boolean birthdayOpen;

    public UserReqDto toDto() {
        return UserReqDto.builder()
                .push(push)
                .name(name)
                .birthday(birthday)
                .solar(solar)
                .birthdayOpen(birthdayOpen)
                .build();
    }

}
