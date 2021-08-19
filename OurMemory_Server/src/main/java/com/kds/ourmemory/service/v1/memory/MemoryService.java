package com.kds.ourmemory.service.v1.memory;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryInternalServerException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundRoomException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundWriterException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.memory.dto.*;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
                                    String.format("Memory '%s' insert failed.", memory.getName())));
                })
                .map(memory -> {
                    // Relation memory and writer
                    memory.getWriter().addMemory(memory);
                    memory.addUser(memory.getWriter());

                    // Relation memory and room
                    var roomId = relationMemoryToRoom(memory, request.getRoomId());

                    return new InsertMemoryDto.Response(memory, roomId);
                })
                .orElseThrow(() -> new MemoryNotFoundWriterException(
                                String.format(NOT_FOUND_MESSAGE, "writer", request.getUserId())
                        )
                );
    }

    @Transactional
    private Long relationMemoryToRoom(Memory memory, Long roomId) {
        return findRoom(roomId)
                .map(room -> {
                    room.addMemory(memory);
                    memory.addRoom(room);

                    room.getUsers().forEach(user ->
                        fcmService.sendMessageTo(
                                FcmDto.Request.builder()
                                        .token(user.getPushToken())
                                        .deviceOs(user.getDeviceOs())
                                        .title("OurMemory - 일정 추가")
                                        .body(String.format("'%s' 일정이 방에 추가되었습니다.", memory.getName()))
                                        .build()
                        )
                    );

                    return roomId;
                })
                .orElse(null);
    }

    // 일정 공유용 기능
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
        return Optional.ofNullable(id).flatMap(userRepo::findById);
    }

    private boolean isDeleteUser(Long id) {
        return userRepo.findById(id)
                .filter(user -> !user.isUsed()).isPresent();
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
