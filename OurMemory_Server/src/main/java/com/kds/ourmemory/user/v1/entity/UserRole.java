package com.kds.ourmemory.user.v1.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("사용자"),
    ADMIN("관리자")
    ;

    private final String desc;
}
