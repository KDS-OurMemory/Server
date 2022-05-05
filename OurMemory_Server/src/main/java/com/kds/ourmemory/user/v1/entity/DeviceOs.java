package com.kds.ourmemory.user.v1.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceOs {
    AOS("Android"),
    IOS("iOS");

    private final String type;
}