package com.kds.ourmemory.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public enum DeviceOs {
    ANDROID("Android"),
    IOS("iOS");

    private static final Map<String, DeviceOs> stringToEnum =
        Stream.of(values()).collect(toMap(Objects::toString, e-> e));

    private final String type;

    @JsonCreator
    public static DeviceOs fromString(String type) {
        return stringToEnum.get(type);
    }

    @JsonValue
    public String getType() {
        return type;
    }
}