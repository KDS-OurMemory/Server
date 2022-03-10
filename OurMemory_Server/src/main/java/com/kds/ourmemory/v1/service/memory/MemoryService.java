package com.kds.ourmemory.v1.service.memory;

import com.kds.ourmemory.v1.advice.memory.exception.*;
import com.kds.ourmemory.v1.advice.relation.exception.UserMemoryInternalServerException;
import com.kds.ourmemory.v1.advice.room.exception.RoomInternalServerException;
import com.kds.ourmemory.v1.advice.room.exception.RoomNotFoundException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotFoundException;
import com.kds.ourmemory.v1.controller.firebase.dto.FcmDto;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryReqDto;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryRspDto;
import com.kds.ourmemory.v1.controller.room.dto.RoomReqDto;
import com.kds.ourmemory.v1.entity.memory.Memory;
import com.kds.ourmemory.v1.entity.relation.UserMemory;
import com.kds.ourmemory.v1.entity.room.Room;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.repository.memory.MemoryRepository;
import com.kds.ourmemory.v1.repository.relation.UserMemoryRepository;
import com.kds.ourmemory.v1.repository.room.RoomRepository;
import com.kds.ourmemory.v1.repository.user.UserRepository;
import com.kds.ourmemory.v1.service.firebase.FcmService;
import com.kds.ourmemory.v1.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class MemoryService {
    private final MemoryRepository memoryRepo;

    // When creating a memory, added because sometimes a room is created
    private final RoomService roomService;

    // Add to work in user table
    private final UserRepository userRepo;

    // Add to work in memory and user relationship tables
    private final UserMemoryRepository userMemoryRepo;

    // Add to work in memory and rooms relationship tables    
    private final RoomRepository roomRepo;

    // Add to FCM
    private final FcmService fcmService;

    @Transactional
    public MemoryRspDto insert(MemoryReqDto reqDto) {
        if (isDeleteUser(reqDto.getUserId()))
            throw new MemoryDeactivateWriterException(reqDto.getUserId());

        return findUser(reqDto.getUserId())
                .map(writer -> insertMemory(reqDto.toEntity(writer))
                        .orElseThrow(() -> new MemoryInternalServerException(
                                        String.format("Memory '%s' insert failed.", reqDto.getName())
                                )
                        )
                )
                .map(memory -> {
                    // Relation memory and private room
                    var roomId = relationMemoryToPrivateRoom(memory, memory.getWriter().getPrivateRoomId());

                    // Share memory to room only not private room
                    if (!memory.getWriter().getPrivateRoomId().equals(reqDto.getRoomId())) {
                        roomId = Optional.ofNullable(reqDto.getRoomId())
                                .map(shareRoomId -> {
                                    shareMemoryToRooms(memory, List.of(shareRoomId));
                                    return shareRoomId;
                                })
                                .orElse(roomId);
                    }

                    return new MemoryRspDto(memory, roomId);
                })
                .orElseThrow(() -> new MemoryNotFoundWriterException(reqDto.getUserId()));
    }

    @Transactional
    private Long relationMemoryToPrivateRoom(Memory memory, Long privateRoomId) {
        return findRoom(privateRoomId)
                .map(room -> {
                    room.addMemory(memory);
                    memory.addRoom(room);

                    return privateRoomId;
                })
                .orElse(null);
    }

    @Transactional
    private void shareMemoryToRooms(Memory memory, List<Long> roomIds) {
        Optional.ofNullable(roomIds)
                .map(List::stream).ifPresent(stream -> stream.forEach(id ->
                        findRoom(id)
                                .map(room -> {
                                    room.addMemory(memory);
                                    memory.addRoom(room);

                                    room.getUsers().forEach(user -> fcmService.sendMessageTo(
                                                    FcmDto.Request.builder()
                                                            .token(user.getPushToken())
                                                            .deviceOs(user.getDeviceOs())
                                                            .title("OurMemory - 일정 공유")
                                                            .body(String.format("'%s' 일정이 방에 공유되었습니다.", memory.getName()))
                                                            .build()

                                            )
                                    );
                                    return room;
                                })
                                .orElseThrow(() -> new MemoryNotFoundRoomException(id))
                ));
    }

    @Transactional
    public MemoryRspDto find(long memoryId, long roomId) {
        return findMemory(memoryId)
                .filter(Memory::isUsed)
                .map(memory -> {
                    var room = findRoom(roomId)
                            .orElseThrow(() -> new RoomNotFoundException(roomId));

                    // Check memory include room
                    if (
                            room.getMemories().stream().filter(rm -> rm.getId() == memoryId).findAny().isEmpty()
                    ) {
                        throw new MemoryNotIncludeRoomException(memoryId, roomId);
                    }

                    var roomMember = room.getUsers();

                    var userMemories = findUserMemoryByMemoryAndUserIn(memory, roomMember)
                            .orElseGet(ArrayList::new);

                    return new MemoryRspDto(memory, userMemories);
                })
                .orElseThrow(() -> new MemoryNotFoundException(memoryId)
                );
    }

    @Transactional
    public List<MemoryRspDto> findMemories(Long writerId, String name) {
        List<Memory> findMemories = new ArrayList<>();

        findMemoriesByWriterId(writerId).ifPresent(findMemories::addAll);
        findMemoriesByName(name).ifPresent(findMemories::addAll);

        var privateRoomId = findUser(writerId).map(User::getPrivateRoomId).orElse(null);

        return findMemories.stream()
                .filter(Memory::isUsed)
                .distinct()
                .sorted(
                        Comparator.comparing(Memory::getStartDate)  // first order
                                .thenComparing(Memory::getRegDate)  // second order
                )
                .map(memory -> new MemoryRspDto(privateRoomId, memory))
                .collect(toList());
    }

    @Transactional
    public MemoryRspDto update(long memoryId, long userId, MemoryReqDto reqDto) {
        var memory = findMemory(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!Objects.equals(memory.getWriter().getId(), user.getId())) {
            throw new MemoryNotWriterException(user.getId(), memory.getWriter().getId());
        }

        return memory.updateMemory(reqDto)
                .map(MemoryRspDto::new)
                .orElseThrow(() -> new MemoryInternalServerException("Failed to update for memory data"));
    }

    @Transactional
    public MemoryRspDto setAttendanceStatus(long memoryId, MemoryReqDto reqDto) {
        var attendMemoryResponse = findUserMemoryByMemoryIdAndUserId(memoryId, reqDto.getUserId())
                .map(userMemory -> {
                    userMemory.updateAttendance(reqDto.getAttendanceStatus());
                    return new MemoryRspDto(userMemory);
                })
                .orElse(null);

        if (attendMemoryResponse != null) {
            return attendMemoryResponse;
        }

        return findMemory(memoryId)
                .map(memory -> {
                    var user = findUser(reqDto.getUserId())
                            .orElseThrow(() -> new UserNotFoundException(reqDto.getUserId()));

                    var userMemory = UserMemory.builder()
                            .user(user)
                            .memory(memory)
                            .status(reqDto.getAttendanceStatus())
                            .build();

                    var insertedUserMemory = insertUserMemory(userMemory)
                            .orElseThrow(() -> new UserMemoryInternalServerException(
                                    String.format("UserMemory [user: %d, memory: %d] insert failed.",
                                            memory.getWriter().getId(), memory.getId())
                            ));

                    user.addMemory(insertedUserMemory);
                    memory.addUser(insertedUserMemory);

                    return new MemoryRspDto(userMemory);
                })
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));
    }

    @Transactional
    public MemoryRspDto shareMemory(long memoryId, long userId, MemoryReqDto reqDto) {
        checkNotNull(reqDto.getShareIds(), "공유 대상 목록이 없습니다. 공유 대상 목록을 입력해주세요.");
        checkNotNull(reqDto.getShareType(), "일정 공유 대상 종류값이 없습니다. 값을 입력해주세요.");

        var memory = findMemory(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        switch (reqDto.getShareType()) {
            case USERS -> reqDto.getShareIds().forEach(id -> findUser(id)
                    .map(target -> {
                        var insertRoomReq = RoomReqDto.builder()
                                .name(user.getName() + ", " + target.getName())
                                .userId(userId)
                                .opened(false)
                                .member(List.of(target.getId()))
                                .build();

                        var insertRoomRsp = roomService.insert(insertRoomReq);
                        return findRoom(insertRoomRsp.getRoomId())
                                .orElseThrow(() -> new RoomNotFoundException(insertRoomRsp.getRoomId()));
                    })
                    .map(room -> {
                        room.addMemory(memory);
                        memory.addRoom(room);

                        return room;
                    })
                    .orElseThrow(() -> new MemoryNotFoundShareMemberException(id)));

            case USER_GROUP -> Optional.of(
                            RoomReqDto.builder()
                                    .name("Share room from " + user.getName())
                                    .userId(userId)
                                    .opened(false)
                                    .member(
                                            reqDto.getShareIds().stream().map(
                                                            memberId -> findUser(memberId).map(User::getId)
                                                                    .orElseThrow(() -> new MemoryNotFoundShareMemberException(memberId))
                                                    )
                                                    .collect(toList())
                                    )
                                    .build()
                    )
                    .map(roomService::insert)
                    .map(insertRoomRsp -> findRoom(insertRoomRsp.getRoomId())
                            .map(room -> {
                                room.addMemory(memory);
                                memory.addRoom(room);

                                return room;
                            })
                            .orElseThrow(RoomInternalServerException::new));
            case ROOMS -> reqDto.getShareIds().forEach(roomId ->
                    findRoom(roomId)
                            .map(room -> {
                                room.addMemory(memory);
                                memory.addRoom(room);

                                return room;
                            })
                            .orElseThrow(() -> new MemoryNotFoundShareRoomException(roomId))
            );
        }

        return new MemoryRspDto(user.getPrivateRoomId(), memory);
    }

    @Transactional
    public MemoryRspDto delete(long memoryId, long userId, long roomId) {
        var memory = findMemory(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(memoryId));

        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 1. Delete memory from private room -> delete memory
        if (user.getPrivateRoomId().equals(roomId)) {
            memory.deleteMemory();
        }
        // 2. Delete memory from share room -> delete room-memory relation
        else {
            var room = findRoom(roomId)
                    .orElseThrow(() -> new RoomNotFoundException(roomId));

            // Check memory include room
            if (
                    room.getMemories().stream().filter(rm -> rm.getId() == memoryId).findAny().isEmpty()
            ) {
                throw new MemoryNotIncludeRoomException(memoryId, roomId);
            }

            room.deleteMemory(memory);
            memory.deleteRoom(room);
        }

        // delete response is null -> client already have data. So don't need response data.
        return null;
    }

    /**
     * Memory Repository
     */
    private Optional<Memory> insertMemory(Memory memory) {
        return Optional.of(memoryRepo.save(memory));
    }

    private Optional<Memory> findMemory(Long id) {
        return Optional.ofNullable(id).flatMap(memoryId -> memoryRepo.findById(memoryId).filter(Memory::isUsed));
    }

    private Optional<List<Memory>> findMemoriesByName(String name) {
        return memoryRepo.findAllByName(name);
    }

    private Optional<List<Memory>> findMemoriesByWriterId(Long userId) {
        return memoryRepo.findAllByWriterId(userId);
    }

    /**
     * User Repository
     * <p>
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepo::findById).filter(User::isUsed);
    }

    private boolean isDeleteUser(Long id) {
        return userRepo.findById(id)
                .filter(user -> !user.isUsed()).isPresent();
    }

    /**
     * UserMemory Repository
     * <p>
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<UserMemory> insertUserMemory(UserMemory userMemory) {
        return Optional.of(userMemoryRepo.save(userMemory));
    }

    private Optional<UserMemory> findUserMemoryByMemoryIdAndUserId(Long memoryId, Long userId) {
        return userMemoryRepo.findByMemoryIdAndUserId(memoryId, userId);
    }

    private Optional<List<UserMemory>> findUserMemoryByMemoryAndUserIn(Memory memory, List<User> users) {
        return userMemoryRepo.findAllByMemoryAndUserIn(memory, users);
    }

    /**
     * Room Repository
     * <p>
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<Room> findRoom(Long id) {
        return Optional.ofNullable(id).flatMap(roomId -> roomRepo.findById(roomId).filter(Room::isUsed));
    }
}
