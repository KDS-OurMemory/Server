package com.kds.ourmemory.controller.v1.user.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PatchUserTokenRequestDto {
    @NotBlank
    @ApiModelProperty(value = "변경할 FCM 푸시토큰 값", required = true)
    private String pushToken;
}
