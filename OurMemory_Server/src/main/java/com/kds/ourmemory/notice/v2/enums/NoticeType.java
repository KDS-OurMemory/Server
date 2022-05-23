package com.kds.ourmemory.notice.v2.enums;

import com.kds.ourmemory.notice.v1.entity.Notice;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@ApiModel
@Getter
@AllArgsConstructor
public enum NoticeType {
    FRIEND_REQUEST("친구 요청")
    ;

    private final String desc;

    public static com.kds.ourmemory.notice.v1.entity.NoticeType toV1(NoticeType noticeType) {
        return Optional.ofNullable(noticeType)
                .map(type -> switch (type) {
                    case FRIEND_REQUEST -> com.kds.ourmemory.notice.v1.entity.NoticeType.FRIEND_REQUEST;
                })
                .orElse(null);
    }

    public static NoticeType toV2(com.kds.ourmemory.notice.v1.entity.NoticeType noticeType) {
        return Optional.ofNullable(noticeType)
                .map(type -> switch (type) {
                    case FRIEND_REQUEST -> NoticeType.FRIEND_REQUEST;
                })
                .orElse(null);
    }

}
