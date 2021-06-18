package com.kds.ourmemory.controller.v1.friend;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.friend.dto.*;
import com.kds.ourmemory.service.v1.friend.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"2. Friend"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/friends")
public class FriendController {
    private final FriendService friendService;

    @ApiOperation(value = "친구 요청", notes = "사용자에게 친구 요청 푸시 알림을 전송한다. 차단한 상대에게는 요청이 전달되지 않는다.")
    @PostMapping(value = "/request")
    public ApiResult<RequestFriendDto.Response> requestFriend(@RequestBody RequestFriendDto.Request request) {
        return ok(friendService.requestFriend(request));
    }

    @ApiOperation(value = "친구 요청 취소", notes = "친구 요청을 취소한다.")
    @DeleteMapping(value = "/cancel")
    public ApiResult<CancelFriendDto.Response> cancelFriend(@RequestBody CancelFriendDto.Request request) {
        return ok(friendService.cancelFriend(request));
    }

    @ApiOperation(value = "친구 수락", notes = "친구 요청을 수락하고 친구를 추가한다. 요청한 사람/요청받은 사람 모두 친구 추가 진행.")
    @PostMapping(value = "/accept")
    public ApiResult<AcceptFriendDto.Response> acceptFriend(@RequestBody AcceptFriendDto.Request request) {
        return ok(friendService.acceptFriend(request));
    }

    @ApiOperation(value = "친구 추가", notes = "요청한 사람쪽에서 친구 추가를 진행한다. ")
    @PostMapping
    public ApiResult<AddFriendDto.Response> addFriend(@RequestBody AddFriendDto.Request request) {
        return ok(friendService.addFriend(request));
    }

    @ApiOperation(value = "친구 목록 조회", notes = "사용자의 친구 목록을 조회한다.")
    @GetMapping(value = "/{userId}")
    public ApiResult<List<FindFriendsDto.Response>> findFriends(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(friendService.findFriends(userId).stream()
                .map(FindFriendsDto.Response::new)
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "친구 차단", notes = "친구 상태를 차단으로 변경한다.")
    @PatchMapping
    public ApiResult<BlockFriendDto.Response> blockFriend(@RequestBody BlockFriendDto.Request request) {
        return ok(friendService.blockFriend(request));
    }

    @ApiOperation(value = "친구 삭제", notes = "친구를 삭제한다. 내 쪽에서만 친구 삭제 처리한다.")
    @DeleteMapping
    public ApiResult<DeleteFriendDto.Response> deleteFriend(@RequestBody DeleteFriendDto.Request request) {
        return ok(friendService.deleteFriend(request));
    }
}
