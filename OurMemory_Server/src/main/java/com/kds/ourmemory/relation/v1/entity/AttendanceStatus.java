package com.kds.ourmemory.relation.v1.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceStatus {
    ATTEND("참석"),
    ABSENCE("불참")
    ;

    private final String desc;
}
