package com.kds.ourmemory.controller.v1.room;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.room.dto.*;
import com.kds.ourmemory.service.v1.room.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

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

    @ApiOperation(value = "방 단일 조회")
    @GetMapping(value = "/{roomId}")
    public ApiResult<RoomRspDto> find(@PathVariable long roomId) {
        return ok(roomService.find(roomId));
    }

    @ApiOperation(value = "방 목록 조회", notes = "조건에 맞는 방 목록을 조회한다.")
    @GetMapping
    public ApiResult<List<RoomRspDto>> findRooms(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String name
    ) {
        return ok(roomService.findRooms(userId, name));
    }

    @ApiOperation(value = "방장 양도", notes = "방 참여자에게 방장을 양도한다.")
    @PatchMapping("/{roomId}/owner/{userId}")
    public ApiResult<RoomRspDto> patchOwner(
            @PathVariable long roomId,
            @PathVariable long userId
    ) {
        return ok(roomService.patchOwner(roomId, userId));
    }

    @ApiOperation(value = "방 정보 수정", notes = "전달받은 값이 있는 경우 수정")
    @PutMapping("/{roomId}")
    public ApiResult<RoomRspDto> update(
        @PathVariable long roomId,
        @RequestParam RoomReqDto reqDto
    ) {
        return ok(roomService.update(roomId, reqDto));
    }

    @ApiOperation(value = "방 삭제", notes = """
            1. 개인방 -> 일정 삭제 후 방 삭제, 2. 공유방 -> 방만 삭제\s
            성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping(value = "/{roomId}")
    public ApiResult<RoomRspDto> delete(
            @PathVariable long roomId,
            @RequestBody RoomReqDto reqDto
    ) {
        return ok(roomService.delete(roomId, reqDto));
    }
}
