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

import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.room.dto.DeleteResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.FindRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertResponseDto;
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
    public ApiResult<InsertResponseDto> insert(@RequestBody InsertRequestDto request) throws CRoomException {
        return ok(roomService.insert(request.toEntity(), request.getMember()));
    }

    @ApiOperation(value = "방 목록 조회", notes = "사용자가 참여중인 방 목록을 조회한다.")
    @GetMapping(value = "/rooms/{snsId}")
    public ApiResult<List<FindRoomResponseDto>> findRooms(
            @ApiParam(value = "snsId", required = true) @PathVariable String snsId) throws CUserNotFoundException {
        return ok(roomService.findRooms(snsId).stream().filter(Room::isUsed).map(FindRoomResponseDto::new)
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "방 삭제", notes = "방 번호에 맞는 방을 삭제한다.")
    @DeleteMapping(value = "/room/{roomId}")
    public ApiResult<DeleteResponseDto> delete(@ApiParam(value = "roomId", required = true) @PathVariable Long roomId)
            throws CRoomException {
        return ok(roomService.delete(roomId));
    }
}
