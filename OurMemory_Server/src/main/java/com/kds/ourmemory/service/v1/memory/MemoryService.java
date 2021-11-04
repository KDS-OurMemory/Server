package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.*;
import com.kds.ourmemory.advice.v1.relation.exception.UserMemoryInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.memory.dto.*;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
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
    public InsertMemoryDto.Response insert(InsertMemoryDto.Request request) {
        if (isDeleteUser(request.getUserId()))
            throw new MemoryNotFoundWriterException(
                    String.format(NOT_FOUND_MESSAGE, "writer", request.getUserId())
            );

        return findUser(request.getUserId())
                .map(writer -> {
                    var memory = Memory.builder()
                            .writer(writer)
                            .name(request.getName())
                            .contents(request.getContents())
                            .place(request.getPlace())
                            .startDate(request.getStartDate())
                            .endDate(request.getEndDate())
                            .firstAlarm(request.getFirstAlarm())
                            .secondAlarm(request.getSecondAlarm())
                            .bgColor(request.getBgColor())
                            .used(true)
                            .build();

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
                    if (!memory.getWriter().getPrivateRoomId().equals(request.getRoomId())) {
                        roomId = Optional.ofNullable(request.getRoomId())
                                .map(shareRoomId -> {
                                    shareMemoryToRooms(memory, Stream.of(shareRoomId).collect(toList()));
                                    return shareRoomId;
                                })
                                .orElse(roomId);
                    }

                    return new InsertMemoryDto.Response(memory, roomId);
                })
                .orElseThrow(() -> new MemoryNotFoundWriterException(
                                String.format(NOT_FOUND_MESSAGE, "writer", request.getUserId())
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

    public FindMemoryDto.Response find(long memoryId, long roomId) {
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

                    return new FindMemoryDto.Response(memory, userMemories);
                })
                .orElseThrow(() -> new MemoryNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId)
                        )
                );
    }

    public List<FindMemoriesDto.Response> findMemories(Long writerId, String name) {
        List<Memory> findMemories = new ArrayList<>();

        findMemoriesByWriterId(writerId).ifPresent(findMemories::addAll);
        findMemoriesByName(name).ifPresent(findMemories::addAll);

        return findMemories.stream()
                .filter(Memory::isUsed)
                .distinct()
                .sorted(
                        Comparator.comparing(Memory::getStartDate)  // first order
                                .thenComparing(Memory::getRegDate)  // second order
                )
                .map(FindMemoriesDto.Response::new)
                .collect(toList());
    }

    @Transactional
    public UpdateMemoryDto.Response update(long memoryId, long userId, UpdateMemoryDto.Request request) {
        var memory = findMemory(memoryId)
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId))
                );

        if (memory.getWriter().getId() != userId) {
            throw new MemoryNotWriterException(
                    String.format("%s must be updated by writer.", MEMORY)
            );
        }

        return memory.updateMemory(request)
                .map(r -> new UpdateMemoryDto.Response())
                .orElseThrow(() -> new MemoryInternalServerException("Failed to update for memory data"));
    }

    @Transactional
    public AttendMemoryDto.Response setAttendanceStatus(long memoryId, long userId, AttendanceStatus status) {
        var attendMemoryResponse = findUserMemoryByMemoryIdAndUserId(memoryId, userId)
                .map(userMemory -> {
                    userMemory.updateAttendance(status);
                    return new AttendMemoryDto.Response();
                })
                .orElse(null);

        if (attendMemoryResponse != null) {
            return attendMemoryResponse;
        }

        return findMemory(memoryId)
                .map(memory -> {
                    var user = findUser(userId)
                            .orElseThrow(() -> new UserNotFoundException(
                                    String.format(NOT_FOUND_MESSAGE, USER, userId)
                            ));

                    var userMemory = UserMemory.builder()
                            .user(user)
                            .memory(memory)
                            .status(status)
                            .build();

                    var insertedUserMemory = insertUserMemory(userMemory)
                            .orElseThrow(() -> new UserMemoryInternalServerException(
                                    String.format("UserMemory [user: %d, memory: %d] insert failed.",
                                            memory.getWriter().getId(), memory.getId())
                            ));

                    user.addMemory(insertedUserMemory);
                    memory.addUser(insertedUserMemory);

                    return new AttendMemoryDto.Response();
                })
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId))
                );
    }

    @Transactional
    public ShareMemoryDto.Response shareMemory(long memoryId, long userId, ShareMemoryDto.Request request) {
        checkNotNull(request.getTargetIds(), "공유 대상 목록이 없습니다. 공유 대상 목록을 입력해주세요.");
        checkNotNull(request.getType(), "일정 공유 대상 종류값이 없습니다. 값을 입력해주세요.");

        var memory = findMemory(memoryId)
                .orElseThrow(() -> new MemoryNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId)
                ));

        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, USER, userId)
                ));

        switch (request.getType()) {
            case USERS -> request.getTargetIds().forEach(id -> findUser(id)
                    .map(target -> {
                        var insertRoomReq = new InsertRoomDto.Request(
                                user.getName() + ", " + target.getName(),
                                userId,
                                false,
                                Stream.of(target.getId()).collect(toList())
                        );
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
                var insertRoomReq = new InsertRoomDto.Request(
                        "Share room from " + user.getName(),
                        userId,
                        false,
                        request.getTargetIds()
                );
                var insertRoomRsp = roomService.insert(insertRoomReq);
                return findRoom(insertRoomRsp.getRoomId())
                        .map(room -> {
                            room.addMemory(memory);
                            memory.addRoom(room);
                            return new ShareMemoryDto.Response();
                        })
                        .orElseThrow(() -> new MemoryInternalServerException(
                                String.format("Memory '%s' insert failed.", memory.getName())
                        ));
            }
            case ROOMS -> request.getTargetIds().forEach(roomId ->
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

        return new ShareMemoryDto.Response();
    }

    @Transactional
    public DeleteMemoryDto.Response delete(long memoryId, DeleteMemoryDto.Request request) {
        var memory = findMemory(memoryId)
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId))
                );

        return findUser(request.getUserId())
                .map(user -> {
                    // 1. Delete memory from private room -> delete memory
                    if (user.getPrivateRoomId() == request.getTargetRoomId()) {
                        memory.deleteMemory();
                    }
                    // 2. Delete memory from share room -> delete room-memory relation
                    else {
                        findRoom(request.getTargetRoomId())
                                .ifPresent(room -> {
                                    room.deleteMemory(memory);
                                    memory.deleteRoom(room);
                                });
                    }

                    return new DeleteMemoryDto.Response();
                })
                .orElseThrow(
                        () -> new UserNotFoundException(String.format(NOT_FOUND_MESSAGE, USER, request.getUserId()))
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
