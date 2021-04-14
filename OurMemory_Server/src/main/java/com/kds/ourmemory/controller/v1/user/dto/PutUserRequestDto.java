package com.kds.ourmemory.controller.v1.user.dto;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PutUserRequestDto {

    @Nullable
    @ApiModelProperty(value = "사용자명", required = false, notes = "null 또는 빈 값일 경우 업데이트 안함.", example = "다다다|null")
    private String name;

    @Nullable
    @Pattern(regexp = "^(0[1-9]|1[0-2])([0-2][0-9]|3[0-1])$")
    @ApiModelProperty(value = "생일", required = false, notes = "MMdd, null 또는 빈 값일 경우 업데이트 안함.", example = "0101|null")
    private String birthday;

    @Nullable
    @ApiModelProperty(value = "생일 공개여부", required = false, notes = "null 또는 빈 값일 경우 업데이트 안함.", example = "true|false|null")
    private Boolean birthdayOpen;

    @Nullable
    @ApiModelProperty(value = "푸시 사용여부", required = false, notes = "null 또는 빈 값일 경우 업데이트 안함.", example = "true|false|null")
    private Boolean push;
}
