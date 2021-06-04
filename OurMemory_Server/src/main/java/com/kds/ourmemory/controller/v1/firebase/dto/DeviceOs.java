package com.kds.ourmemory.controller.v1.firebase.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel(value = "Fcm.DeviceOs", description = "enum class in FcmDto")
@Getter
@AllArgsConstructor
public enum DeviceOs {
    Android("Android"),
    iOS("iOS");

    private final String type;
}