package com.kds.ourmemory.controller.v1.friend;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.friend.dto.DeleteFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.FindFriendsDto;
import com.kds.ourmemory.controller.v1.friend.dto.InsertFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.RequestFriendDto;
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
    public ApiResult<RequestFriendDto.Response> request(
            @RequestBody RequestFriendDto.Request request) {
        return ok(friendService.requestFriend(request));
    }

    @ApiOperation(value = "친구 추가", notes = "전달받은 사용자를 친구 목록에 추가한다.")
    @PostMapping
    public ApiResult<InsertFriendDto.Response> insert(@RequestBody InsertFriendDto.Request request) {
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

    @ApiOperation(value = "친구 삭제", notes = "친구를 삭제한다. 내 쪽에서만 친구 삭제 처리한다.")
    @DeleteMapping(value = "/{userId}")
    public ApiResult<DeleteFriendDto.Response> delete(
            @ApiParam(value = "userId", required = true) @PathVariable long userId,
            @RequestBody DeleteFriendDto.Request request) {
        return ok(friendService.delete(userId, request));
    }
}
