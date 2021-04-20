package com.kds.ourmemory.service.v1.memory;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryInternalServerException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundRoomException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundWriterException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryDto;
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

@RequiredArgsConstructor
@Service
public class MemoryService {
    
    // When creating a memory, added because sometimes a room is created
    private final RoomService roomService;
    
    private final MemoryRepository memoryRepo;

    // Add to work in memory and user relationship tables
    private final UserRepository userRepo;
    
    // Add to work in memory and rooms relationship tables    
    private final RoomRepository roomRepo;
    
    private final FcmService fcmService;
    
    @Transactional
    public InsertMemoryDto.Response insert(InsertMemoryDto.Request request) {
        checkNotNull(request.getUserId(), "일정 작성자 번호가 입력되지 않았습니다. 일정을 생성하는 사용자의 번호를 입력해주세요.");
        checkNotNull(request.getName(), "일정 제목이 입력되지 않았습니다. 일정 제목을 입력해주세요.");
        
        checkNotNull(request.getStartDate(), "일정 시작시간이 입력되지 않았습니다. 일정 시작시간을 입력해주세요.");
        checkNotNull(request.getEndDate(), "일정 종료시간이 입력되지 않았습니다. 일정 종료시간을 입력해주세요.");
        
        checkNotNull(request.getBgColor(), "일정 배경색이 지정되지 않았습니다. 배경색을 지정해주세요.");
        
        return findUser(request.getUserId())
                .map(writer -> {
                    Memory memory = Memory.builder()
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
                    relationMemoryToRoom(memory, request.getShareRooms());
                    Long roomId = relationMainRoom(memory, request);
                    
                    return new InsertMemoryDto.Response(memory.getId(), roomId, memory.getRegDate());
                })
                .orElseThrow(() -> new MemoryNotFoundWriterException(
                        "Not found writer matched to userId: " + request.getUserId()));
    }
    
    @Transactional
    private void relationMemoryToRoom(Memory memory, List<Long> roomIds) {
        Optional.ofNullable(roomIds)
        .map(List::stream).ifPresent(stream -> stream.forEach(id -> 
            findRoom(id)
            .filter(Objects::nonNull)
            .map(room -> {
                room.addMemory(memory);
                memory.addRoom(room);

                room.getUsers().stream()
                        .forEach(user -> fcmService.sendMessageTo(new FcmRequestDto(user.getPushToken(), user.getDeviceOs(),
                                "OurMemory - 일정 공유", String.format("'%s' 일정이 방에 공유되었습니다.", memory.getName()))));
                return room;
            })
            .orElseThrow(() -> new MemoryNotFoundRoomException("Not found room matched roomId: " + id))
        ));
    }
    
    @Transactional
    private void relationMemoryToMembers(Memory memory, List<Long> members) {
        Optional.ofNullable(members).map(List::stream)
                .ifPresent(stream -> stream.forEach(id -> findUser(id).filter(Objects::nonNull).map(user -> {
                    user.addMemory(memory);
                    memory.addUser(user);

                    fcmService.sendMessageTo(new FcmRequestDto(user.getPushToken(), user.getDeviceOs(),
                            "OurMemory - 일정 공유", String.format("'%s' 일정에 참여되셨습니다.", memory.getName())));
                    return user;
                }).orElseThrow(() -> new MemoryNotFoundRoomException("Not found room matched roomId: " + id))));
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
     * @param memory
     * @param request 
     */
    private Long relationMainRoom(Memory memory, InsertMemoryDto.Request request) {
        Room mainRoom = findRoom(request.getRoomId())
                // 1. If there is a main room -> Ensure that all participants are included
                .filter(room -> {
                    List<Long> memoryMembers = memory.getUsers().stream().map(User::getId).collect(Collectors.toList());

                    return room.getUsers().stream().map(User::getId).collect(Collectors.toList())
                            .containsAll(memoryMembers);
                })
                // 1-2) Participants are not included in the main room OR 2. If there is no main room
                .orElseGet(() -> Optional.ofNullable(request.getMembers())
                        // 1) If there are participants -> Create room and push notification
                        .map(members -> {
                            // Create make room protocol
                            List<User> users = memory.getUsers();
                            String name = StringUtils
                                    .join(users.stream().map(User::getName).collect(Collectors.toList()), ", ");
                            Long owner = memory.getWriter().getId();
                            InsertRoomDto.Request insertRoomRequestDto = new InsertRoomDto.Request(name, owner, false,
                                    request.getMembers());
                            
                            // make room
                            InsertRoomDto.Response insertRoomResponseDto = roomService.insert(insertRoomRequestDto);

                            // push message
                            return findRoom(insertRoomResponseDto.getRoomId()).map(room -> {
                                room.getUsers().stream().forEach(
                                        user -> fcmService.sendMessageTo(new FcmRequestDto(user.getPushToken(), user.getDeviceOs(),
                                                "OurMemory - 방 생성", String.format("일정 '%s' 을 공유하기 위한 방 '%s' 가 생성되었습니다.",
                                                        memory.getName(), room.getName()))));
                                return room;
                            }).orElseThrow(() -> new MemoryNotFoundRoomException(
                                    String.format("Unable to find a room to include the memory '%s'. roomId: %d",
                                            memory.getName(), insertRoomResponseDto.getRoomId())));
                        })
                        // If no participants are present
                        .orElse(null));

        return Optional.ofNullable(mainRoom)
                // If a room exists to associate with the memory
                .map(room -> {
                    relationMemoryToRoom(memory, Arrays.asList(room.getId()));
                    return room.getId();
                }).orElse(null);
    }
    
    public List<Memory> findMemorys(long userId) {
        return findUser(userId)
                .map(User::getMemorys)
                .orElseThrow(() -> new MemoryNotFoundWriterException("Not found writer from userId: " + userId));
    }
    
    @Transactional
    public DeleteMemoryDto.Response deleteMemory(long id) {
        return findMemory(id)
                .map(memory -> {
                    memory.getRooms().stream().forEach(room -> room.getMemorys().remove(memory));
                    memory.getUsers().stream().forEach(user -> user.getMemorys().remove(memory));
                    deleteMemory(memory);
                    return new DeleteMemoryDto.Response(BaseTimeEntity.CLocalDateTime.formatTime(LocalDateTime.now()));
                })
                .orElseThrow(() -> new MemoryNotFoundException("Not found memory matched to memoryid: " + id));
    }
    
    /**
     * Memory Repository 
     */
    private Optional<Memory> insertMemory(Memory memory) {
        return Optional.ofNullable(memoryRepo.save(memory));
    }
    
    private Optional<Memory> findMemory(Long id) {
        return Optional.ofNullable(id)
                .map(memoryRepo::findById)
                .orElseGet(Optional::empty);
    }
    
    private void deleteMemory(Memory memory) {
        Optional.ofNullable(memory)
            .ifPresent(memoryRepo::delete);
    }
    
    /**
     * User Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id)
                .map(userRepo::findById)
                .orElseGet(Optional::empty);
    }
    
    /**
     * Room Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<Room> findRoom(Long id) {
        return Optional.ofNullable(id)
                .map(roomRepo::findById)
                .orElseGet(Optional::empty);
    }
}
