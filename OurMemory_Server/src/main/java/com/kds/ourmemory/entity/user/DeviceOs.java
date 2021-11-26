package com.kds.ourmemory.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceOs {
    AOS("Android"),
    IOS("iOS");

    private final String type;
}