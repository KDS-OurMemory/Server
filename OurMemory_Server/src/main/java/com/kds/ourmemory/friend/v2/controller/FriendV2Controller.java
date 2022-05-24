package com.kds.ourmemory.friend.v2.controller;

import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.friend.v2.controller.dto.*;
import com.kds.ourmemory.friend.v2.enums.FriendStatus;
import com.kds.ourmemory.friend.v2.service.FriendV2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.common.v1.controller.ApiResult.ok;

@Api(tags = {"2-2. Friend V2"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v2/friends")
public class FriendV2Controller {

    private final FriendV2Service friendV2Service;

    @ApiOperation(value = "친구 요청", notes = """
            사용자에게 친구 요청 푸시 알림을 전송한다. 차단한 상대에게는 요청이 전달되지 않는다.
            상대방 기준 사용자와 이미 친구라면 친구 요청을 수락할 수 없기 때문에 F006 응답코드를 전달한다.
            """)
    @PostMapping("/request")
    public ApiResult<FriendRequestRspDto> requestFriend(@RequestBody FriendRequestReqDto reqDto) {
        return ok(friendV2Service.requestFriend(reqDto));
    }

    @ApiOperation(value = "친구 요청 취소",
            notes = "친구 요청을 취소한 뒤, 요청보낸 사용자의 친구요청 알림을 삭제한다. " +
                    "성공한 경우, 실제 친구 데이터가 삭제되기 때문에 response=null 을 리턴한다.")
    @DeleteMapping("/cancel/users/{userId}/friendUsers/{friendUserId}")
    public ApiResult<FriendCancelRequestRspDto> cancelRequest(
            @ApiParam(value = "사용자 번호") @PathVariable long userId,
            @ApiParam(value = "요청 취소할 사용자 번호") @PathVariable long friendUserId
    ) {
        return ok(friendV2Service.cancelRequest(userId, friendUserId));
    }

    @ApiOperation(value = "친구 수락",
            notes = "친구 요청을 수락하고 친구를 추가한다. 요청을 수락한 사용자 알림 중 친구 요청 알림을 읽음처리한다."
    )
    @PostMapping("/accept")
    public ApiResult<FriendAcceptRequestRspDto> acceptRequest(@RequestBody FriendAcceptRequestReqDto reqDto) {
        return ok(friendV2Service.acceptRequest(reqDto));
    }

    @ApiOperation(value = "친구 목록 조회", notes = "사용자의 친구 목록을 조회한다.")
    @GetMapping("/{userId}")
    public ApiResult<List<FriendFindFriendsRspDto>> findFriends(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(friendV2Service.findFriends(userId));
    }

    @ApiOperation(value = "친구 상태 변경", notes = "친구 상태를 변경한다. 요청(WAIT)/수락 대기(REQUESTED_BY) 상태로는 변경할 수 없다.")
    @PatchMapping("/status")
    public ApiResult<FriendPatchFriendStatusRspDto> patchFriendStatus(@RequestBody FriendPatchFriendStatusReqDto reqDto) {
        return ok(friendV2Service.patchFriendStatus(reqDto));
    }

    @ApiOperation(value = "친구 삭제", notes = """
            친구를 삭제한다. 내 쪽에서만 친구 삭제 처리한다.\s
            "성공한 경우, 실제 친구 데이터가 삭제되기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("users/{userId}/friendUsers/{friendUserId}")
    public ApiResult<FriendDeleteRspDto> delete(
            @ApiParam(value = "사용자 번호") @PathVariable long userId,
            @ApiParam(value = "친구 사용자 번호(삭제할 사용자 번호)") @PathVariable long friendUserId

    ) {
        return ok(friendV2Service.delete(userId, friendUserId));
    }

    @ApiOperation(value = "친구 재 추가", notes = "친구 요청 후 상대방이 이미 친구인 경우, 내 쪽에서만 친구 추가를 진행한다.")
    @PostMapping("/reAdd")
    public ApiResult<FriendReAddRspDto> reAdd(@RequestBody FriendReAddReqDto reqDto) {
        return ok(friendV2Service.reAdd(reqDto));
    }

    @ApiOperation(value = "사용자 검색", notes = "조건에 해당하는 사용자의 기본 정보와 친구 상태를 검색한다.")
    @GetMapping("/users/{userId}/search")
    public ApiResult<List<FriendFindUsersRspDto>> findUsers(
            @PathVariable long userId,
            @ApiParam(value = "대상 사용자 번호") @RequestParam(required = false) Long targetId,
            @ApiParam(value = "대상 사용자 이름") @RequestParam(required = false) String name,
            @ApiParam(value = "검색 사용자 기준 대상 사용자의 친구상태") @RequestParam(required = false) FriendStatus friendStatus
    ) {
        return ok(friendV2Service.findUsers(userId, targetId, name, friendStatus));
    }

}
