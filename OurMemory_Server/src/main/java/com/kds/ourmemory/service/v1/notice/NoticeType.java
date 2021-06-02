package com.kds.ourmemory.service.v1.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeType {
    FRIEND_REQUEST("친구 요청")
    ;

    private final String desc;
}
