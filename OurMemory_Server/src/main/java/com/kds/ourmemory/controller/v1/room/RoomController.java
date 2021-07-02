package com.kds.ourmemory.controller.v1.room;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.FindRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.FindRoomsDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.service.v1.room.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = { "3. Room" })
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    @ApiOperation(value = "방 생성", notes = "앱에서 전달받은 데이터로 방 생성 및 사용자 추가")
    @PostMapping
    public ApiResult<InsertRoomDto.Response> insert(@RequestBody InsertRoomDto.Request request) {
        return ok(roomService.insert(request));
    }

    @ApiOperation(value = "방 개별 조회")
    @GetMapping(value = "/{roomId}")
    public ApiResult<FindRoomDto.Response> find(@PathVariable long roomId) {
        return ok(roomService.find(roomId));
    }


    @ApiOperation(value = "방 목록 조회", notes = "조건에 맞는 방 목록을 조회한다.")
    @GetMapping
    public ApiResult<List<FindRoomsDto.Response>> findRooms(
            @RequestParam Long userId,
            @RequestParam String name
    ) {
        return ok(roomService.findRooms(userId, name).stream()
                .filter(Room::isUsed)
                .map(FindRoomsDto.Response::new)
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "방 삭제", notes = "방 삭제, 사용자-방-일정 연결된 관계 삭제")
    @DeleteMapping(value = "/{roomId}")
    public ApiResult<DeleteRoomDto.Response> delete(
            @ApiParam(value = "roomId", required = true) @PathVariable long roomId) {
        return ok(roomService.delete(roomId));
    }
}
