package com.kds.ourmemory.user.v2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum DeviceOs {
    AOS("Android"),
    IOS("iOS");

    private final String type;

    public static com.kds.ourmemory.user.v1.entity.DeviceOs toV1(DeviceOs deviceOs) {
        return Optional.ofNullable(deviceOs)
                .map(os -> switch (os) {
                    case AOS -> com.kds.ourmemory.user.v1.entity.DeviceOs.AOS;
                    case IOS -> com.kds.ourmemory.user.v1.entity.DeviceOs.IOS;
                })
                .orElse(null);
    }

}
