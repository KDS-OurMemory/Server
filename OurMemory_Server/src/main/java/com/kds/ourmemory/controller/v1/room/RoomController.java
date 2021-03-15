package com.kds.ourmemory.controller.v1.room;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.room.dto.RoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.RoomResponseDto;
import com.kds.ourmemory.service.v1.room.RoomService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@Api(tags = {"2. Room"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class RoomController {

    private final RoomService roomService;
    
    @ApiOperation(value="방 생성", notes = "앱에서 전달받은 데이터로 방 생성 및 사용자 추가")
    @PostMapping(value="/room")
    public ApiResult<RoomResponseDto> createRoom(@RequestBody RoomRequestDto request) throws CRoomException {
        return ok(roomService.createRoom(request.toEntity(), request.getMember()));
    }
}
