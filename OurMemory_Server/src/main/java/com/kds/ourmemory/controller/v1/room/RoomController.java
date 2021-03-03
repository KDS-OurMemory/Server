package com.kds.ourmemory.controller.v1.room;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.dto.room.RoomRequestDto;
import com.kds.ourmemory.dto.room.RoomResponseDto;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController(value = "/v1")
public class RoomController {

    private final RoomService roomService;
    
    @PostMapping(value="/room")
    public RoomResponseDto createRoom(@RequestBody RoomRequestDto request) {
        return roomService.createRoom(request.toEntity(), request.getMember());
    }
}
