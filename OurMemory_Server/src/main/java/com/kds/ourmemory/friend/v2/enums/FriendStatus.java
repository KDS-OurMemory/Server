package com.kds.ourmemory.friend.v2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum FriendStatus {
    WAIT("친구 요청 후 대기상태"),
    REQUESTED_BY("친구 요청을 받은 상태"),
    FRIEND("친구 추가 완료된 상태, 상대방에서 친구 삭제한 여부는 파악할 수 없음"),
    BLOCK("차단한 상태")
    ;

    private final String desc;

    public static com.kds.ourmemory.friend.v1.entity.FriendStatus toV1(FriendStatus friendStatus) {
        return Optional.ofNullable(friendStatus)
                .map(status -> switch (status) {
                    case WAIT -> com.kds.ourmemory.friend.v1.entity.FriendStatus.WAIT;
                    case REQUESTED_BY -> com.kds.ourmemory.friend.v1.entity.FriendStatus.REQUESTED_BY;
                    case FRIEND -> com.kds.ourmemory.friend.v1.entity.FriendStatus.FRIEND;
                    case BLOCK -> com.kds.ourmemory.friend.v1.entity.FriendStatus.BLOCK;
                })
                .orElse(null);
    }

    public static FriendStatus toV2(com.kds.ourmemory.friend.v1.entity.FriendStatus friendStatus) {
        return Optional.ofNullable(friendStatus)
                .map(status -> switch (status) {
                    case WAIT -> FriendStatus.WAIT;
                    case REQUESTED_BY -> FriendStatus.REQUESTED_BY;
                    case FRIEND -> FriendStatus.FRIEND;
                    case BLOCK -> FriendStatus.BLOCK;
                })
                .orElse(null);
    }

}
