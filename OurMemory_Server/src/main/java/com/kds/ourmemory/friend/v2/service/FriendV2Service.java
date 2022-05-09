package com.kds.ourmemory.friend.v2.service;

import com.kds.ourmemory.friend.v1.entity.FriendStatus;
import com.kds.ourmemory.friend.v1.service.FriendService;
import com.kds.ourmemory.friend.v2.controller.dto.*;
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

    public List<FriendFindUsersRspDto> findUsers(long userId, Long targetId, String name, FriendStatus friendStatus) {
        return friendService.findUsers(userId, targetId, name, friendStatus)
                .stream().map(FriendFindUsersRspDto::new).toList();
    }

    public FriendCancelRequestRspDto cancelRequest(long userId, long friendUserId) {
        return new FriendCancelRequestRspDto(friendService.cancelFriend(userId, friendUserId));
    }

    public FriendAcceptRequestRspDto acceptRequest(FriendAcceptRequestReqDto reqDto) {
        return new FriendAcceptRequestRspDto(friendService.acceptFriend(reqDto.toDto()));
    }

    public FriendReAddRspDto reAdd(FriendReAddReqDto reqDto) {
        return new FriendReAddRspDto(friendService.reAddFriend(reqDto.toDto()));
    }

    public List<FriendFindFriendRspDto> findFriends(long userId) {
        return friendService.findFriends(userId).stream().map(FriendFindFriendRspDto::new).toList();
    }

    public FriendPatchFriendStatusRspDto patchFriendStatus(FriendPatchFriendStatusReqDto reqDto) {
        return new FriendPatchFriendStatusRspDto(friendService.patchFriendStatus(reqDto.toDto()));
    }

    public FriendDeleteRspDto delete(long userId, long friendUserId) {
        return new FriendDeleteRspDto(friendService.deleteFriend(userId, friendUserId));
    }

}
