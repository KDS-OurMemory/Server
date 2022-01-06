package com.kds.ourmemory.v1.service.room;

import com.kds.ourmemory.v1.advice.room.exception.*;
import com.kds.ourmemory.v1.advice.user.exception.UserInternalServerException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotFoundException;
import com.kds.ourmemory.v1.controller.dto.FcmDto;
import com.kds.ourmemory.v1.controller.room.dto.RoomReqDto;
import com.kds.ourmemory.v1.controller.room.dto.RoomRspDto;
import com.kds.ourmemory.v1.entity.memory.Memory;
import com.kds.ourmemory.v1.entity.room.Room;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.repository.room.RoomRepository;
import com.kds.ourmemory.v1.repository.user.UserRepository;
import com.kds.ourmemory.v1.service.firebase.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Transactional
    public RoomRspDto insert(RoomReqDto reqDto) {
        return findUser(reqDto.getUserId())
                .map(owner -> insertRoom(reqDto.toEntity(owner))
                            .orElseThrow(() -> new RoomInternalServerException(String.format(
                                    "Insert room failed. [name: %s, owner: %s]", reqDto.getName(), owner.getName())))
                )
                .map(room -> {
                    // Relation room and owner
                    room.getOwner().addRoom(room);
                    room.addUser(room.getOwner());

                    // Relation room and members
                    return addMemberToRoom(room, reqDto.getMember());
                })
                .map(RoomRspDto::new)
                .orElseThrow(() -> new RoomNotFoundOwnerException(reqDto.getUserId()));
    }

    private Room addMemberToRoom(Room room, List<Long> members) {
        Optional.ofNullable(members)
                .ifPresent(mem -> mem.forEach(id ->
                    findUser(id).map(user -> {
                        user.addRoom(room);
                        room.addUser(user);

                        fcmService.sendMessageTo(
                            new FcmDto.Request(
                                    user.getPushToken(), user.getDeviceOs(),
                                    "OurMemory - 방 참여", String.format("'%s' 방에 초대되셨습니다.", room.getName())
                            )
                        );
                        return user;
                    })
                    .orElseThrow(() -> new RoomNotFoundMemberException(id))
                )
        );

        return room;
    }

    @Transactional
    public Long insertPrivateRoom(Long userId) {
        return findUser(userId)
                .map(user -> {
                    var privateRoom = Room.builder()
                            .name(user.getName())
                            .opened(false)
                            .owner(user)
                            .build();
                    return insertRoom(privateRoom)
                            .map(room -> {
                                room.addUser(user);
                                user.addRoom(room);

                                return room.getId();
                            })
                            .orElseThrow(() -> new UserInternalServerException(
                                    String.format("%s '%s' insert failed.", "memory", privateRoom.getName())
                            ));
                })
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public RoomRspDto find(Long roomId) {
        var room = findRoom(roomId)
                .filter(Room::isUsed)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        return new RoomRspDto(room);
    }

    public List<RoomRspDto> findRooms(Long userId, String name) {
        List<Room> findRooms = new ArrayList<>();

        findUser(userId).ifPresent(
                user -> findRooms.addAll(
                        user.getRooms().stream().filter(
                                        room -> room.isUsed() && !room.getId().equals(user.getPrivateRoomId())
                                )
                                .collect(toList())
                )
        );
        findRoomsByName(name).ifPresent(
                rooms -> findRooms.addAll(rooms.stream().filter(Room::isUsed).collect(toList()))
        );

        return findRooms.stream()
                .distinct()
                .sorted(Comparator.comparing(Room::getRegDate).reversed())
                .map(RoomRspDto::new)
                .collect(toList());
    }

    @Transactional
    public RoomRspDto patchOwner(long roomId, long userId) {
        return findRoom(roomId)
                .map(room -> {
                    long beforeOwnerId = room.getOwner().getId();

                    for (var member : room.getUsers()) {
                        Optional.of(member).filter(m -> m.getId() == userId)
                                .ifPresent(owner -> {
                                    if (room.getOwner().equals(owner)) {
                                        throw new RoomAlreadyOwnerException(userId);
                                    }
                                    room.patchOwner(owner);
                                });
                    }
                    long afterOwnerId = room.getOwner().getId();

                    if (beforeOwnerId == afterOwnerId) {
                        throw new RoomNotFoundMemberException(userId);
                    }

                    return room;
                })
                .map(RoomRspDto::new)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    @Transactional
    public RoomRspDto update(long roomId, RoomReqDto reqDto) {
        return findRoom(roomId).map(room ->
                        room.updateRoom(reqDto)
                                .map(RoomRspDto::new)
                                .orElseThrow(() -> new RoomInternalServerException("Failed to update for room data."))
                )
                .orElseThrow(
                        () -> new RoomNotFoundException(roomId)
                );
    }

    @Transactional
    public RoomRspDto delete(long roomId, long userId) {
        var privateRoomId = findUser(userId)
                .map(User::getPrivateRoomId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        findRoom(roomId)
                // 1. delete room
                .map(Room::deleteRoom)
                .map(room -> {
                    // 2. delete memories -> private room only
                    if (room.getId().longValue() == privateRoomId) {
                        room.getMemories().forEach(Memory::deleteMemory);
                    }

                    return true;
                })
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        // delete response is null -> client already have data, so don't need response data.
        return null;
    }

    /**
     * Room Repository
     */
    private Optional<Room> insertRoom(Room room) {
        return Optional.of(roomRepo.save(room));
    }

    private Optional<Room> findRoom(Long id) {
        return Optional.ofNullable(id).flatMap(roomId -> roomRepo.findById(roomId).filter(Room::isUsed));
    }

    private Optional<List<Room>> findRoomsByName(String name) {
        return roomRepo.findAllByName(name);
    }

    /**
     * User Repository
     * <p>
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userId -> userRepo.findById(userId).filter(User::isUsed));
    }

}
