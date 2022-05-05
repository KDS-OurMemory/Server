package com.kds.ourmemory.room.v1.controller;

import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.room.v1.controller.dto.RoomReqDto;
import com.kds.ourmemory.room.v1.controller.dto.RoomRspDto;
import com.kds.ourmemory.room.v1.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.common.v1.controller.ApiResult.ok;

@Api(tags = { "3. Room" })
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    @ApiOperation(value = "방 생성", notes = "앱에서 전달받은 데이터로 방 생성 및 사용자 추가")
    @PostMapping
    public ApiResult<RoomRspDto> insert(@RequestBody RoomReqDto reqDto) {
        return ok(roomService.insert(reqDto));
    }

    @ApiOperation(value = "방 단일 조회", notes = "방 정보, 방에 포함된 일정을 조회한다.")
    @GetMapping("/{roomId}")
    public ApiResult<RoomRspDto> find(@PathVariable long roomId) {
        return ok(roomService.find(roomId));
    }

    @ApiOperation(value = "방 목록 조회", notes = """
            조건에 맞는 방 목록을 조회한다. 개인방은 보여지지 않는다.
            각 조건은 OR 검색된다.""")
    @GetMapping
    public ApiResult<List<RoomRspDto>> findRooms(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String name
    ) {
        return ok(roomService.findRooms(userId, name));
    }

    @ApiOperation(value = "방장 양도", notes = "방 참여자에게 방장을 양도한다.")
    @PatchMapping("/{roomId}/owner/{userId}")
    public ApiResult<RoomRspDto> recommendOwner(
            @PathVariable long roomId,
            @PathVariable long userId
    ) {
        return ok(roomService.recommendOwner(roomId, userId));
    }

    @ApiOperation(value = "방 정보 수정", notes = "전달받은 값이 있는 경우 수정")
    @PutMapping("/{roomId}")
    public ApiResult<RoomRspDto> update(
        @PathVariable long roomId,
        @RequestBody RoomReqDto reqDto
    ) {
        return ok(roomService.update(roomId, reqDto));
    }

    @ApiOperation(value = "방 삭제", notes = """
            1. 개인방
                1) 일정 삭제 후 방 삭제
            2. 공유방
                1) 방만 삭제

            * 성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("/{roomId}/users/{userId}")
    public ApiResult<RoomRspDto> delete(
            @PathVariable long roomId,
            @ApiParam(value = "방장 번호") @PathVariable long userId
    ) {
        return ok(roomService.delete(roomId, userId));
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
    public ApiResult<RoomRspDto> exit(
            @ApiParam(value = "방 번호") @PathVariable long roomId,
            @ApiParam(value = "사용자 번호") @PathVariable long userId,
            @ApiParam(value = "방장을 위임할 사용자 번호") @RequestParam Long recommendUserId
    ) {
        return ok(roomService.exit(roomId, userId, recommendUserId));
    }

}
