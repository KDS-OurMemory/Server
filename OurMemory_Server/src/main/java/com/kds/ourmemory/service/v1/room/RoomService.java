package com.kds.ourmemory.service.v1.room;

import com.kds.ourmemory.advice.v1.room.exception.*;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.room.dto.*;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepo;
    
    // Add to work in rooms and user relationship tables
    private final UserRepository userRepo;

    // Add to FCM
    private final FcmService fcmService;

    private static final String NOT_FOUND_MESSAGE = "Not found '%s' matched id: %d";
    private static final String ALREADY_OWNER_MESSAGE = "User '%d' is already owner room '%d'.";

    @Transactional
    public InsertRoomDto.Response insert(InsertRoomDto.Request request) {
        return findUser(request.getOwner())
                .map(owner -> {
                    var room = Room.builder()
                            .owner(owner)
                            .name(request.getName())
                            .opened(request.isOpened())
                            .used(true)
                            .build();
                    return insertRoom(room)
                            .orElseThrow(() -> new RoomInternalServerException(String.format(
                                    "Insert room failed. [name: %s, owner: %s]", request.getName(), owner.getName())));
                })
                .map(room -> {
                    // Relation room and owner
                    room.getOwner().addRoom(room);
                    room.addUser(room.getOwner());

                    // Relation room and members
                    return addMemberToRoom(room, request.getMember());
                })
                .map(InsertRoomDto.Response::new)
                .orElseThrow(() -> new RoomNotFoundOwnerException(
                                String.format(NOT_FOUND_MESSAGE, "user", request.getOwner())
                        )
                );
    }

    private Room addMemberToRoom(Room room, List<Long> members) {
        Optional.ofNullable(members)
                .ifPresent(mem -> mem.forEach(id ->
                        findUser(id).map(user -> {
                                user.addRoom(room);
                                room.addUser(user);

                                fcmService.sendMessageTo(new FcmDto.Request(user.getPushToken(), user.getDeviceOs(),
                                        "OurMemory - 방 참여", String.format("'%s' 방에 초대되셨습니다.", room.getName())));
                                return user;
                        })
                        .orElseThrow(() -> new RoomNotFoundMemberException(
                                        String.format(NOT_FOUND_MESSAGE, "member", id)
                                )
                        )
                )
        );
        
        return room;
    }

    public FindRoomDto.Response find(Long roomId) {
        return findRoom(roomId)
                .filter(Room::isUsed)
                .map(FindRoomDto.Response::new)
                .orElseThrow(() -> new RoomNotFoundException(
                            String.format(NOT_FOUND_MESSAGE, "room", roomId)
                        )
                );
    }

    public List<Room> findRooms(Long userId, String name) {
        List<Room> findRooms = new ArrayList<>();

        findUser(userId).ifPresent(
                user -> findRooms.addAll(user.getRooms().stream().filter(Room::isUsed).collect(toList()))
        );
        findRoomsByName(name).ifPresent(
                rooms -> findRooms.addAll(rooms.stream().filter(Room::isUsed).collect(toList()))
        );

        return findRooms;
    }

    @Transactional
    public PatchRoomOwnerDto.Response patchOwner(long roomId, long userId) {
        return findRoom(roomId)
                .map(room -> {
                    long beforeOwnerId = room.getOwner().getId();

                    for (var member: room.getUsers()) {
                        Optional.of(member).filter(m -> m.getId() == userId)
                                .ifPresent(owner -> {
                                    if (room.getOwner().equals(owner)) {
                                        throw new RoomAlreadyOwnerException(
                                                String.format(ALREADY_OWNER_MESSAGE, userId, roomId)
                                        );
                                    }
                                    room.patchOwner(owner);
                                });
                    }
                    long afterOwnerId = room.getOwner().getId();

                    if (beforeOwnerId == afterOwnerId) {
                        throw new RoomNotFoundMemberException(
                                String.format(NOT_FOUND_MESSAGE, "member", userId)
                        );
                    }

                    return room;
                })
                .map(room -> new PatchRoomOwnerDto.Response(BaseTimeEntity.formatNow()))
                .orElseThrow(() -> new RoomNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, "room", roomId)
                    )
                );
    }

    @Transactional
    public UpdateRoomDto.Response update(long roomId, UpdateRoomDto.Request request) {
        return findRoom(roomId).map(room ->
                room.updateRoom(request)
                        .map(r -> new UpdateRoomDto.Response(r.formatModDate()))
                        .orElseThrow(() -> new RoomInternalServerException("Failed to update for room data."))
        )
        .orElseThrow(
                () -> new RoomNotFoundException(String.format(NOT_FOUND_MESSAGE, "room", roomId))
        );
    }

    @Transactional
    public DeleteRoomDto.Response delete(long id) {
        return findRoom(id)
                .map(Room::deleteRoom)
                .map(room -> {
                    room.getMemories().forEach(room::deleteMemory);
                    return room;
                })
                .map(room -> new DeleteRoomDto.Response(BaseTimeEntity.formatNow()))
                .orElseThrow(() -> new RoomNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, "room", id)
                        )
                );
    }
    
    /**
     * Room Repository
     */
    private Optional<Room> insertRoom(Room room) {
        return Optional.of(roomRepo.save(room));
    }
    
    private Optional<Room> findRoom(Long id) {
        return Optional.ofNullable(id).flatMap(roomRepo::findById);
    }

    private Optional<List<Room>> findRoomsByName(String name) {
        return roomRepo.findAllByName(name);
    }

    /**
     * User Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepo::findById);
    }
}
