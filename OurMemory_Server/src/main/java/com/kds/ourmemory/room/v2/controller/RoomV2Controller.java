package com.kds.ourmemory.room.v2.controller;

import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.room.v2.controller.dto.*;
import com.kds.ourmemory.room.v2.service.RoomV2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.common.v1.controller.ApiResult.ok;

@Api(tags = { "3-2. Room" })
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v2/rooms")
public class RoomV2Controller {

    private final RoomV2Service roomV2Service;

    @ApiOperation(value = "방 생성", notes = "앱에서 전달받은 데이터로 방 생성 및 사용자 추가")
    @PostMapping
    public ApiResult<RoomInsertRspDto> insert(@RequestBody RoomInsertReqDto reqDto) {
        return ok(roomV2Service.insert(reqDto));
    }

    @ApiOperation(value = "방 단일 조회", notes = "방 정보, 방에 포함된 일정을 조회한다.")
    @GetMapping("/{roomId}")
    public ApiResult<RoomFindRspDto> find(@PathVariable long roomId) {
        return ok(roomV2Service.find(roomId));
    }

    @ApiOperation(value = "방 목록 조회", notes = """
            조건에 맞는 방 목록을 조회한다. 개인방은 보여지지 않는다.
            각 조건은 OR 검색된다.""")
    @GetMapping
    public ApiResult<List<RoomFindRspDto>> findRooms(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String name
    ) {
        return ok(roomV2Service.findRooms(userId, name));
    }

    @ApiOperation(value = "방장 양도", notes = "방 참여자에게 방장을 양도한다.")
    @PatchMapping("/{roomId}/owner/{userId}")
    public ApiResult<RoomRecommendOwnerRspDto> recommendOwner(
            @PathVariable long roomId,
            @PathVariable long userId
    ) {
        return ok(roomV2Service.recommendOwner(roomId, userId));
    }

    @ApiOperation(value = "방 정보 수정", notes = "전달받은 값이 있는 경우 수정")
    @PutMapping("/{roomId}")
    public ApiResult<RoomUpdateRspDto> update(
            @PathVariable long roomId,
            @RequestBody RoomUpdateReqDto reqDto
    ) {
        return ok(roomV2Service.update(roomId, reqDto));
    }

    @ApiOperation(value = "방 삭제", notes = """
            1. 개인방
                1) 일정 삭제 후 방 삭제
            2. 공유방
                1) 방만 삭제

            * 성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("/{roomId}/owner/{userId}")
    public ApiResult<Void> delete(
            @PathVariable long roomId,
            @ApiParam(value = "방장 번호") @PathVariable long ownerId
    ) {
        return ok(roomV2Service.delete(roomId, ownerId));
    }

    @ApiOperation(value = "방 나가기", notes = """
            1. 공유방
                1-1. 방장인 경우
                    1) 방장 위임(위임할 사용자번호가 없는 경우, 임의로 위임됨.)
                    2) 방 나가기(방-사용자 관계 삭제)
                1-2. 참여자인 경우
                    1) 방 나가기(방-사용자 관계 삭제)
            2. 개인방
                1) 공유된 일정 삭제(방-일정 관계 삭제)
                2) 방 나가기(방-사용자 관계 삭제)
                3) 방 삭제
            성공 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("/{roomId}/exit/{userId}")
    public ApiResult<Void> exit(
            @ApiParam(value = "방 번호") @PathVariable long roomId,
            @ApiParam(value = "사용자 번호") @PathVariable long userId,
            @ApiParam(value = "방장을 위임할 사용자 번호") @RequestParam Long recommendUserId
    ) {
        return ok(roomV2Service.exit(roomId, userId, recommendUserId));
    }

}
