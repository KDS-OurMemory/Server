package com.kds.ourmemory.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public enum UserRole {
    USER("user", "사용자"),
    ADMIN("admin","관리자")
    ;

    private static final Map<String, UserRole> stringToEnum =
            Stream.of(values()).collect(toMap(Objects::toString, e-> e));

    private final String name;
    private final String desc;

    @JsonCreator
    public static UserRole fromString(String role) {
        return stringToEnum.get(role);
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
