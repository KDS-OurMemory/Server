package com.kds.ourmemory.entity.friend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FriendStatus {
    WAIT("친구 요청 후 대기상태"),
    FRIEND("친구 추가 완료된 상태, 상대방에서 친구 삭제한 여부는 파악할 수 없음"),
    BLOCK("내가 차단한 사람")
    ;

    private final String desc;
}
