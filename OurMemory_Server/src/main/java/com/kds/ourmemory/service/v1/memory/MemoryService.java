package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.*;
import com.kds.ourmemory.advice.v1.relation.exception.UserMemoryInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryReqDto;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryRspDto;
import com.kds.ourmemory.controller.v1.room.dto.RoomReqDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.relation.UserMemory;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.memory.MemoryRepository;
import com.kds.ourmemory.repository.relation.UserMemoryRepository;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FcmService;
import com.kds.ourmemory.service.v1.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    private static final String NOT_FOUND_MESSAGE = "Not found %s matched id: %d";

    private static final String MEMORY = "memory";

    private static final String USER = "user";

    private static final String ROOM = "room";

    @Transactional
    public MemoryRspDto insert(MemoryReqDto reqDto) {
        if (isDeleteUser(reqDto.getUserId()))
            throw new MemoryNotFoundWriterException(
                    String.format(NOT_FOUND_MESSAGE, "writer", reqDto.getUserId())
            );

        return findUser(reqDto.getUserId())
                .map(writer -> {
                    var memory = reqDto.toEntity(writer);

                    return insertMemory(memory)
                            .orElseThrow(() -> new MemoryInternalServerException(
                                        String.format("Memory '%s' insert failed.", memory.getName())
                                    )
                            );
                })
                .map(memory -> {
                    // Relation memory and private room
                    var roomId = relationMemoryToPrivateRoom(memory, memory.getWriter().getPrivateRoomId());

                    // Share memory to room only not private room
                    if (!memory.getWriter().getPrivateRoomId().equals(reqDto.getRoomId())) {
                        roomId = Optional.ofNullable(reqDto.getRoomId())
                                .map(shareRoomId -> {
                                    shareMemoryToRooms(memory, Stream.of(shareRoomId).collect(toList()));
                                    return shareRoomId;
                                })
                                .orElse(roomId);
                    }

                    return new MemoryRspDto(memory, roomId);
                })
                .orElseThrow(() -> new MemoryNotFoundWriterException(
                                String.format(NOT_FOUND_MESSAGE, "writer", reqDto.getUserId())
                        )
                );
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
                        .orElseThrow(() -> new MemoryNotFoundRoomException(
                                        String.format(NOT_FOUND_MESSAGE, ROOM, id)
                                )
                        )
        ));
    }

    public MemoryRspDto find(long memoryId, long roomId) {
        return findMemory(memoryId)
                .filter(Memory::isUsed)
                .map(memory -> {
                    var roomMember = findRoom(roomId)
                            .map(Room::getUsers)
                            .orElseThrow(() -> new RoomNotFoundException(
                                    String.format(NOT_FOUND_MESSAGE, ROOM, roomId)
                            ));

                    var userMemories = findUserMemoryByMemoryAndUserIn(memory, roomMember)
                            .orElseGet(ArrayList::new);

                    return new MemoryRspDto(memory, userMemories);
                })
                .orElseThrow(() -> new MemoryNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId)
                        )
                );
    }

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
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId))
                );

        if (memory.getWriter().getId() != userId) {
            throw new MemoryNotWriterException(
                    String.format("%s must be updated by writer.", MEMORY)
            );
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
                            .orElseThrow(() -> new UserNotFoundException(
                                    String.format(NOT_FOUND_MESSAGE, USER, reqDto.getUserId())
                            ));

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
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId))
                );
    }

    @Transactional
    public MemoryRspDto shareMemory(long memoryId, long userId, MemoryReqDto reqDto) {
        checkNotNull(reqDto.getShareIds(), "공유 대상 목록이 없습니다. 공유 대상 목록을 입력해주세요.");
        checkNotNull(reqDto.getShareType(), "일정 공유 대상 종류값이 없습니다. 값을 입력해주세요.");

        var memory = findMemory(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId)
                ));

        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, USER, userId)
                ));

        switch (reqDto.getShareType()) {
            case USERS -> reqDto.getShareIds().forEach(id -> findUser(id)
                    .map(target -> {
                        var insertRoomReq = RoomReqDto.builder()
                                .name(user.getName() + ", " + target.getName())
                                .userId(userId)
                                .opened(false)
                                .member(Stream.of(target.getId()).collect(toList()))
                                .build();

                        var insertRoomRsp = roomService.insert(insertRoomReq);
                        return findRoom(insertRoomRsp.getRoomId())
                                .orElseThrow(() -> new RoomNotFoundException(
                                        String.format(NOT_FOUND_MESSAGE, "insertedRoom", insertRoomRsp.getRoomId())
                                ));
                    })
                    .map(room -> {
                        room.addMemory(memory);
                        memory.addRoom(room);

                        return room;
                    })
                    .orElseThrow(() -> new MemoryNotFoundShareMemberException(
                            String.format(NOT_FOUND_MESSAGE, "shareMember", id)
                    )));

            case USER_GROUP -> {
                var insertRoomReq = RoomReqDto.builder()
                        .name("Share room from " + user.getName())
                        .userId(userId)
                        .opened(false)
                        .member(reqDto.getShareIds())
                        .build();
                var insertRoomRsp = roomService.insert(insertRoomReq);
                findRoom(insertRoomRsp.getRoomId())
                        .map(room -> {
                            room.addMemory(memory);
                            memory.addRoom(room);

                            return room;
                        })
                        .orElseThrow(() -> new MemoryInternalServerException(
                                String.format("Memory '%s' insert failed.", memory.getName())
                        ));
            }
            case ROOMS -> reqDto.getShareIds().forEach(roomId ->
                    findRoom(roomId)
                            .map(room -> {
                                room.addMemory(memory);
                                memory.addRoom(room);

                                return room;
                            })
                            .orElseThrow(() -> new RoomNotFoundException(
                                    String.format(NOT_FOUND_MESSAGE, "shareRoom", roomId)
                            ))
            );
        }

        return new MemoryRspDto(user.getPrivateRoomId(), memory);
    }

    @Transactional
    public MemoryRspDto delete(long memoryId, MemoryReqDto reqDto) {
        var memory = findMemory(memoryId)
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId))
                );

        return findUser(reqDto.getUserId())
                .map(user -> {
                    // 1. Delete memory from private room -> delete memory
                    if (user.getPrivateRoomId() == reqDto.getTargetRoomId()) {
                        memory.deleteMemory();
                    }
                    // 2. Delete memory from share room -> delete room-memory relation
                    else {
                        findRoom(reqDto.getTargetRoomId())
                                .ifPresent(room -> {
                                    room.deleteMemory(memory);
                                    memory.deleteRoom(room);
                                });
                    }

                    return new MemoryRspDto(memory);
                })
                .orElseThrow(
                        () -> new UserNotFoundException(String.format(NOT_FOUND_MESSAGE, USER, reqDto.getUserId()))
                );
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
     * 
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
     *
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
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<Room> findRoom(Long id) {
        return Optional.ofNullable(id).flatMap(roomId -> roomRepo.findById(roomId).filter(Room::isUsed));
    }
}
