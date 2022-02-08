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
import java.util.*;

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

    @Transactional
    public RoomRspDto find(Long roomId) {
        var room = findRoom(roomId)
                .filter(Room::isUsed)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        return new RoomRspDto(room);
    }

    @Transactional
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
    public RoomRspDto recommendOwner(long roomId, long userId) {
        return findRoom(roomId)
                .map(room -> {
                    long beforeOwnerId = room.getOwner().getId();

                    for (var member : room.getUsers()) {
                        Optional.of(member).filter(m -> m.getId() == userId)
                                .ifPresent(owner -> {
                                    if (room.getOwner().equals(owner)) {
                                        throw new RoomAlreadyOwnerException(userId, roomId);
                                    }
                                    room.recommendOwner(owner);
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

        var room = findRoom(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (room.getOwner().getId() != userId) {
            throw new RoomNotOwnerException(userId, roomId);
        }

        // 1. delete memories -> private room only
        if (room.getId().longValue() == privateRoomId) {
            room.getMemories().forEach(Memory::deleteMemory);
        }

        // 2. delete room-memory relation
        for (var memory : room.getMemories()) {
            memory.deleteRoom(room);
        }

        var deleteList = List.copyOf(room.getMemories());
        for (var memory : deleteList) {
            room.deleteMemory(memory);
        }

        // 3. delete room
        room.deleteRoom();

        // delete response is null -> client already have data, so don't need response data.
        return null;
    }

    @Transactional
    public RoomRspDto exit(long roomId, long userId, Long recommendUserId) {
        var room = findRoom(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!room.getUsers().contains(user)) {
            throw new RoomNotParticipantException(userId, roomId);
        }

        /* 1. share room's participant */
        if (!Objects.equals(room.getOwner(), user)) {
            // 1-1. exit room(delete room-user relation)
            room.deleteUser(user);
            user.deleteRooms(List.of(room));
        }
        /* 2. room's owner */
        // 2-1. share room
        else if (room.getUsers().size() > 1) {
            // 1) recommend owner when exit owner
            User recommendUser;
            if (recommendUserId != null) {
                recommendUser = findUser(recommendUserId)
                        .orElseThrow(() -> new RoomNotFoundRecommendUserException(recommendUserId));

                if (Objects.equals(recommendUser.getId(), room.getOwner().getId())) {
                    throw new RoomAlreadyOwnerException(recommendUserId, roomId);
                }

                if (!room.getUsers().contains(recommendUser)) {
                    throw new RoomNotParticipantException(recommendUserId, roomId);
                }
            }
            // info) Not present recommendUserId -> random recommend(only exists participants)
            else {
                recommendUser = room.getUsers().stream().filter(u -> u.getId() != userId).collect(toList()).get(0);
            }
            room.recommendOwner(recommendUser);

            // 2) exit room(delete room-user relation)
            room.deleteUser(user);
            user.deleteRooms(List.of(room));
        }
        // 2-2. personal room (Not privateRoom, only member 1)
        else {
            // 1) delete room-memory relation
            for (var memory : room.getMemories()) {
                memory.deleteRoom(room);
            }

            var deleteList = List.copyOf(room.getMemories());
            for (var memory : deleteList) {
                room.deleteMemory(memory);
            }

            // 2) exit room(delete room-user relation)
            room.deleteUser(user);
            user.deleteRooms(List.of(room));

            // 3) delete room
            room.deleteRoom();
        }

        // info) exit response is null -> client already have data, so don't need response data.
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
