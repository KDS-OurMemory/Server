package com.kds.ourmemory.controller.v1.friend;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.friend.dto.FriendReqDto;
import com.kds.ourmemory.controller.v1.friend.dto.FriendRspDto;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.service.v1.friend.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"2. Friend"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/friends")
public class FriendController {
    private final FriendService friendService;

    @ApiOperation(value = "사용자 검색", notes = "조건에 해당하는 사용자의 기본 정보와 친구 상태를 검색한다.")
    @GetMapping(value = "/users/{userId}/search")
    public ApiResult<List<FriendRspDto>> findUsers(
            @PathVariable long userId,
            @ApiParam(value = "대상 사용자 번호") @RequestParam(required = false) Long targetId,
            @ApiParam(value = "대상 사용자 이름") @RequestParam(required = false) String name,
            @ApiParam(value = "검색 사용자 기준 대상 사용자의 친구상태") @RequestParam(required = false) FriendStatus friendStatus
    ) {
        return ok(friendService.findUsers(userId, targetId, name, friendStatus));
    }

    @ApiOperation(value = "친구 요청", notes = "사용자에게 친구 요청 푸시 알림을 전송한다. 차단한 상대에게는 요청이 전달되지 않는다.")
    @PostMapping(value = "/reqDto")
    public ApiResult<FriendRspDto> requestFriend(@RequestBody FriendReqDto reqDto) {
        return ok(friendService.requestFriend(reqDto));
    }

    @ApiOperation(value = "친구 요청 취소",
            notes = "친구 요청을 취소한 뒤, 요청보낸 사용자의 친구요청 알림을 삭제한다. " +
                    "성공한 경우, 실제 친구 데이터가 삭제되기 때문에 response=null 을 리턴한다.")
    @DeleteMapping(value = "/cancel")
    public ApiResult<FriendRspDto> cancelFriend(@RequestBody FriendReqDto reqDto) {
        return ok(friendService.cancelFriend(reqDto));
    }

    @ApiOperation(value = "친구 수락",
            notes = "친구 요청을 수락하고 친구를 추가한다. 요청을 수락한 사용자 알림 중 친구 요청 알림을 읽음처리한다."
    )
    @PostMapping(value = "/accept")
    public ApiResult<FriendRspDto> acceptFriend(@RequestBody FriendReqDto reqDto) {
        return ok(friendService.acceptFriend(reqDto));
    }

    @ApiOperation(value = "친구 재 추가", notes = "친구 요청 후 상대방이 이미 친구인 경우, 내 쪽에서만 친구 추가를 진행한다.")
    @PostMapping(value = "/reAdd")
    public ApiResult<FriendRspDto> reAddFriend(@RequestBody FriendReqDto reqDto) {
        return ok(friendService.reAddFriend(reqDto));
    }

    @ApiOperation(value = "친구 목록 조회", notes = "사용자의 친구 목록을 조회한다.")
    @GetMapping(value = "/{userId}")
    public ApiResult<List<FriendRspDto>> findFriends(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(friendService.findFriends(userId));
    }

    @ApiOperation(value = "친구 상태 변경", notes = "친구 상태를 변경한다. 요청(WAIT)/수락 대기(REQUESTED_BY) 상태로는 변경할 수 없다.")
    @PatchMapping(value = "/status")
    public ApiResult<FriendRspDto> patchFriendStatus(@RequestBody FriendReqDto reqDto) {
        return ok(friendService.patchFriendStatus(reqDto));
    }

    @ApiOperation(value = "친구 삭제", notes = """
            친구를 삭제한다. 내 쪽에서만 친구 삭제 처리한다.\s
            "성공한 경우, 실제 친구 데이터가 삭제되기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping
    public ApiResult<FriendRspDto> deleteFriend(@RequestBody FriendReqDto reqDto) {
        return ok(friendService.deleteFriend(reqDto));
    }
}
