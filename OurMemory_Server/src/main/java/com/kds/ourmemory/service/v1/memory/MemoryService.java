package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryInternalServerException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundRoomException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundWriterException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.memory.dto.*;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.memory.MemoryRepository;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FcmService;
import com.kds.ourmemory.service.v1.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class MemoryService {
    private final MemoryRepository memoryRepo;

    // When creating a memory, added because sometimes a room is created
    private final RoomService roomService;

    // Add to work in memory and user relationship tables
    private final UserRepository userRepo;
    
    // Add to work in memory and rooms relationship tables    
    private final RoomRepository roomRepo;

    // Add to FCM
    private final FcmService fcmService;

    private static final String NOT_FOUND_MESSAGE = "Not found %s matched id: %d";
    
    @Transactional
    public InsertMemoryDto.Response insert(InsertMemoryDto.Request request) {
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
                                    String.format("Memory '%s' insert failed.", memory.getName())));
                })
                .map(memory -> {
                    // Relation memory and writer
                    memory.getWriter().addMemory(memory);
                    memory.addUser(memory.getWriter());

                    // Relation memory and members
                    relationMemoryToMembers(memory, request.getMembers());

                    // Relation memory and share rooms
                    relationMemoryToRooms(memory, request.getShareRooms());

                    // Relation memory and main room
                    var roomId = relationMainRoom(memory, request);

                    return new InsertMemoryDto.Response(memory, roomId);
                })
                .orElseThrow(() -> new MemoryNotFoundWriterException(
                                String.format(NOT_FOUND_MESSAGE, "writer", request.getUserId())
                        )
                );
    }

    @Transactional
    private void relationMemoryToRooms(Memory memory, List<Long> roomIds) {
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
    
    @Transactional
    private void relationMemoryToMembers(Memory memory, List<Long> members) {
        Optional.ofNullable(members)
                .ifPresent(mem -> mem.forEach(id -> findUser(id).map(user -> {
                            user.addMemory(memory);
                            memory.addUser(user);

                            fcmService.sendMessageTo(new FcmDto.Request(user.getPushToken(), user.getDeviceOs(),
                                    "OurMemory - 일정 공유", String.format("'%s' 일정에 참여되셨습니다.", memory.getName())));
                            return user;
                        })
                        .orElseThrow(() -> new MemoryNotFoundRoomException(
                                String.format(NOT_FOUND_MESSAGE, "room", id))
                        )
                )
        );
    }

    /**
     * Set main room to include memory
    *
    * 1. If there is a main room
    *   1) If there are no participants or all rooms are included -> Create room X, memory-to-room connection
    *   2) If participants are not included in the room -> Create room and push notification, memory - room connection
    *
    * 2. If there is no main room
    *   1) If there are participants -> Create room and push notification, memory-to-room connection
    *   2) If no participants are present -> Memory is connect to user and treated as personal memory.
     * 
     * @param memory targetMemory
     * @param request Request Data
     */
    private Long relationMainRoom(Memory memory, InsertMemoryDto.Request request) {
        var mainRoom = findRoom(request.getRoomId())
                // 1. If there is a main room -> Ensure that all participants are included
                .filter(room -> {
                    List<Long> memoryMembers = memory.getUsers().stream().map(User::getId).collect(toList());

                    return room.getUsers().stream().map(User::getId).collect(toList())
                            .containsAll(memoryMembers);
                })
                // 1-2) Participants are not included in the main room OR 2. If there is no main room
                .orElseGet(() -> Optional.ofNullable(request.getMembers()).filter(members -> !members.isEmpty())
                        // 1-2) 2-1) If there are participants -> Create room and push notification
                        .map(members -> {
                            // Create make room protocol
                            List<User> users = memory.getUsers();
                            var name = StringUtils
                                    .join(users.stream().map(User::getName).collect(toList()), ", ");
                            Long owner = memory.getWriter().getId();
                            var insertRoomRequestDto = new InsertRoomDto.Request(name, owner, false,
                                    request.getMembers());

                            // make room
                            var insertRoomResponseDto = roomService.insert(insertRoomRequestDto);

                            // push message
                            return findRoom(insertRoomResponseDto.getRoomId()).map(room -> {
                                room.getUsers().forEach(
                                        user -> fcmService.sendMessageTo(
                                                FcmDto.Request.builder()
                                                        .token(user.getPushToken())
                                                        .deviceOs(user.getDeviceOs())
                                                        .title("OurMemory - 방 생성")
                                                        .body(String.format("일정 '%s' 을 공유하기 위한 방 '%s' 가 생성되었습니다.",
                                                                memory.getName(), room.getName()))
                                                        .build())
                                );

                                return room;
                            }).orElseThrow(() -> new MemoryNotFoundRoomException(
                                            String.format(NOT_FOUND_MESSAGE, "room", insertRoomResponseDto.getRoomId())
                                    )
                            );
                        })
                        // 2-2) If no participants are present
                        .orElse(null)
                );

        return Optional.ofNullable(mainRoom)
                // If a room exists to associate with the memory
                .map(room -> {
                    relationMemoryToRooms(memory, Collections.singletonList(room.getId()));
                    return room.getId();
                }).orElse(null);
    }

    public FindMemoryDto.Response find(long id) {
        return findMemory(id)
                .filter(Memory::isUsed)
                .map(FindMemoryDto.Response::new)
                .orElseThrow(() -> new MemoryNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, "memory", id)
                        )
                );
    }

    public List<FindMemoriesDto.Response> findMemories(Long userId, String name) {
        List<Memory> findMemories = new ArrayList<>();

        findUser(userId).ifPresent(user -> findMemories.addAll(
                user.getMemories().stream().filter(Memory::isUsed).collect(toList()))
        );
        findMemoriesByName(name).ifPresent(findMemories::addAll);

        return findMemories.stream()
                .filter(Memory::isUsed)
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
                () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, "memory", memoryId))
        );
    }

    @Transactional
    public DeleteMemoryDto.Response delete(long id) {
        return findMemory(id)
                .map(Memory::deleteMemory)
                .map(memory -> new DeleteMemoryDto.Response(BaseTimeEntity.formatNow()))
                .orElseThrow(
                        () -> new MemoryNotFoundException(String.format(NOT_FOUND_MESSAGE, "memory", id))
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

    /**
     * User Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userId -> userRepo.findById(userId).filter(User::isUsed));
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
