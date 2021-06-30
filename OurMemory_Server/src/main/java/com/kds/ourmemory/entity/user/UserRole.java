package com.kds.ourmemory.entity.user;

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
