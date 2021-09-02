package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.*;
import com.kds.ourmemory.advice.v1.relation.exception.UserMemoryInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.memory.dto.*;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
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

                    // Share memory to room
                    roomId = Optional.ofNullable(request.getRoomId())
                            .map(shareRoomId -> {
                                shareMemoryToRooms(memory, Stream.of(shareRoomId).collect(toList()));
                                return shareRoomId;
                            })
                            .orElse(roomId);

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
                                        String.format(NOT_FOUND_MESSAGE, "room", id)
                                )
                        )
        ));
    }

    public FindMemoryDto.Response find(long id) {
        return findMemory(id)
                .filter(Memory::isUsed)
                .map(FindMemoryDto.Response::new)
                .orElseThrow(() -> new MemoryNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, MEMORY, id)
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
                .sorted(Comparator.comparing(Memory::getStartDate)) // first order
                .sorted(Comparator.comparing(Memory::getEndDate))   // second order
                .map(FindMemoriesDto.Response::new)
                .collect(toList());
    }

    @Transactional
    public UpdateMemoryDto.Response update(long memoryId, UpdateMemoryDto.Request request) {
        return findMemory(memoryId).map(memory ->
                memory.updateMemory(request)
                .map(r -> new UpdateMemoryDto.Response(r.formatModDate()))
                .orElseThrow(() -> new MemoryInternalServerException("Failed to update for memory data"))
        )
        .orElseThrow(
                () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, memoryId))
        );
    }

    @Transactional
    public AttendMemoryDto.Response setAttendanceStatus(long memoryId, long userId, AttendanceStatus status) {
        var attendMemoryResponse = findByMemoryIdAndUserId(memoryId, userId)
                .map(userMemory -> {
                    userMemory.updateAttendance(status);
                    return new AttendMemoryDto.Response(BaseTimeEntity.formatNow());
                })
                .orElse(null);

        if (attendMemoryResponse != null) {
            return attendMemoryResponse;
        }

        return findMemory(memoryId)
                .map(memory -> {
                    var user = findUser(userId)
                            .orElseThrow(() -> new UserNotFoundException(
                                    String.format(NOT_FOUND_MESSAGE, "user", userId)
                            ));

                    var userMemory = UserMemory.builder()
                            .user(user)
                            .memory(memory)
                            .status(status)
                            .build();

                    return insertUserMemory(userMemory)
                            .map(um -> new AttendMemoryDto.Response(BaseTimeEntity.formatNow()))
                            .orElseThrow(() -> new UserMemoryInternalServerException(
                                    String.format("UserMemory [user: %d, memory: %d] insert failed.",
                                            memory.getWriter().getId(), memory.getId())
                            ));
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
                        String.format(NOT_FOUND_MESSAGE, "user", userId)
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
                            return new ShareMemoryDto.Response(BaseTimeEntity.formatNow());
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

        return new ShareMemoryDto.Response(BaseTimeEntity.formatNow());
    }

    @Transactional
    public DeleteMemoryDto.Response delete(long id) {
        return findMemory(id)
                .map(Memory::deleteMemory)
                .map(memory -> new DeleteMemoryDto.Response(BaseTimeEntity.formatNow()))
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, MEMORY, id))
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

    private Optional<UserMemory> findByMemoryIdAndUserId(Long memoryId, Long userId) {
        return userMemoryRepo.findByMemoryIdAndUserId(memoryId, userId);
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
