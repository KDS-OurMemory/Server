package com.kds.ourmemory.friend.v2.service;

import com.kds.ourmemory.friend.v1.service.FriendService;
import com.kds.ourmemory.friend.v2.controller.dto.*;
import com.kds.ourmemory.friend.v2.enums.FriendStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FriendV2Service {

    private final FriendService friendService;

    public FriendRequestRspDto requestFriend(FriendRequestReqDto reqDto) {
        return new FriendRequestRspDto(friendService.requestFriend(reqDto.toDto()));
    }

    public FriendCancelRequestRspDto cancelRequest(long userId, long friendUserId) {
        return new FriendCancelRequestRspDto(friendService.cancelFriend(userId, friendUserId));
    }

    public FriendAcceptRequestRspDto acceptRequest(FriendAcceptRequestReqDto reqDto) {
        return new FriendAcceptRequestRspDto(friendService.acceptFriend(reqDto.toDto()));
    }

    public List<FriendFindFriendsRspDto> findFriends(long userId) {
        return friendService.findFriends(userId).stream().map(FriendFindFriendsRspDto::new).toList();
    }

    public FriendPatchFriendStatusRspDto patchFriendStatus(FriendPatchFriendStatusReqDto reqDto) {
        return new FriendPatchFriendStatusRspDto(friendService.patchFriendStatus(reqDto.toDto()));
    }

    public FriendDeleteRspDto delete(long userId, long friendUserId) {
        return new FriendDeleteRspDto(friendService.deleteFriend(userId, friendUserId));
    }

    public FriendReAddRspDto reAdd(FriendReAddReqDto reqDto) {
        return new FriendReAddRspDto(friendService.reAddFriend(reqDto.toDto()));
    }

    public List<FriendFindUsersRspDto> findUsers(long userId, Long targetId, String name, FriendStatus friendStatus) {
        return friendService.findUsers(userId, targetId, name, FriendStatus.toV1(friendStatus))
                .stream().map(FriendFindUsersRspDto::new).toList();
    }

}
