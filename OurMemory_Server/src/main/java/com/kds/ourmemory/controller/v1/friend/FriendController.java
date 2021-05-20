package com.kds.ourmemory.controller.v1.friend;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.friend.dto.FindFriendsDto;
import com.kds.ourmemory.controller.v1.friend.dto.InsertFriendDto;
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
@RequestMapping(value = "/v1")
public class FriendController {
    private final FriendService friendService;

    @ApiOperation(value = "친구 추가", notes = "전달받은 사용자를 친구 목록에 추가한다.")
    @PostMapping(value = "/friend/{userId}")
    public ApiResult<InsertFriendDto.Response> addFriend(
            @ApiParam(value = "userId", required = true) @PathVariable long userId,
            @RequestBody InsertFriendDto.Request request) {
        return ok(friendService.addFriend(userId, request));
    }

    @ApiOperation(value = "친구 목록 조회", notes = "사용자의 친구 목록을 조회한다.")
    @GetMapping(value = "/friends/{userId}")
    public ApiResult<List<FindFriendsDto.Response>> findFriends(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(friendService.findFriends(userId).stream()
                .map(FindFriendsDto.Response::new)
                .collect(Collectors.toList()));
    }
}
