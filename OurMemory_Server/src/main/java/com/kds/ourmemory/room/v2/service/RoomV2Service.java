package com.kds.ourmemory.room.v2.service;

import com.kds.ourmemory.room.v1.service.RoomService;
import com.kds.ourmemory.room.v2.controller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RoomV2Service {

    private final RoomService roomService;

    public RoomInsertRspDto insert(RoomInsertReqDto reqDto) {
        return new RoomInsertRspDto(roomService.insert(reqDto.toDto()));
    }

    public RoomFindRspDto find(long roomId) {
        return new RoomFindRspDto(roomService.find(roomId));
    }

    public List<RoomFindRspDto> findRooms(Long userId, String name) {
        return roomService.findRooms(userId, name)
                .stream().map(RoomFindRspDto::new)
                .toList();
    }

    public RoomRecommendOwnerRspDto recommendOwner(long roomId, long userId) {
        return new RoomRecommendOwnerRspDto(roomService.recommendOwner(roomId, userId));
    }

    public RoomUpdateRspDto update(long roomId, RoomUpdateReqDto reqDto) {
        return new RoomUpdateRspDto(roomService.update(roomId, reqDto.toDto()));
    }

    public Void delete(long roomId, long ownerId) {
        roomService.delete(roomId, ownerId);
        return null;
    }

    public Void exit(long roomId, long userId, Long recommendUserId) {
        roomService.exit(roomId, userId, recommendUserId);
        return null;
    }

}
