package com.kds.ourmemory.notice.v1.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel
@Getter
@AllArgsConstructor
public enum NoticeType {
    FRIEND_REQUEST("친구 요청")
    ;

    private final String desc;
}
