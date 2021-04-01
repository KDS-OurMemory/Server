package com.kds.ourmemory.controller.v1.user.dto;

import lombok.Getter;

@Getter
public class PatchUserTokenRequestDto {
    private String pushToken;
}
