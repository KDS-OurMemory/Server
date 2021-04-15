package com.kds.ourmemory.controller.v1.room;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.FindRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.service.v1.room.RoomService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@Api(tags = { "2. Room" })
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class RoomController {

    private final RoomService roomService;

    @ApiOperation(value = "방 생성", notes = "앱에서 전달받은 데이터로 방 생성 및 사용자 추가")
    @PostMapping(value = "/room")
    public ApiResult<InsertRoomResponseDto> insert(@RequestBody InsertRoomRequestDto request) {
        return ok(roomService.insert(request));
    }

    @ApiOperation(value = "방 목록 조회", notes = "사용자가 참여중인 방 목록을 조회한다.")
    @GetMapping(value = "/rooms/{userId}")
    public ApiResult<List<FindRoomResponseDto>> findRooms(
            @ApiParam(value = "userId", required = true) @PathVariable Long userId) {
        return ok(roomService.findRooms(userId).stream()
                .filter(Room::isUsed)
                .map(FindRoomResponseDto::new)
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "방 삭제", notes = "방 삭제, 사용자-방-일정 연결된 관계 삭제")
    @DeleteMapping(value = "/room/{roomId}")
    public ApiResult<DeleteRoomResponseDto> delete(
            @ApiParam(value = "roomId", required = true) @PathVariable Long roomId) {
        return ok(roomService.delete(roomId));
    }
}
