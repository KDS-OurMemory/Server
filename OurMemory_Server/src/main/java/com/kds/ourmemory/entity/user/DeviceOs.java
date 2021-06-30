package com.kds.ourmemory.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceOs {
    ANDROID("Android"),
    IOS("iOS");

    private final String type;
}