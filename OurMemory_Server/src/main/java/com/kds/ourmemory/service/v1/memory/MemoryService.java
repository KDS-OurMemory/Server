package com.kds.ourmemory.service.v1.memory;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static com.kds.ourmemory.util.DateUtil.currentTime;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.memory.exception.MemoryDataRelationException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryInternalServerException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundRoomException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundWriterException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryResponseDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
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
    
    // 일정 생성 시, 방이 생성되는 경우가 있기 때문에 추가함
    private final RoomService roomService;
    
    private final UserRepository userRepo;
    private final MemoryRepository memoryRepo;
    private final RoomRepository roomRepo;
    
    private final FcmService firebaseFcm;
    
    @Transactional
    public InsertMemoryResponseDto insert(InsertMemoryRequestDto request)
            throws MemoryNotFoundWriterException, MemoryDataRelationException, MemoryInternalServerException {
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
                        .regDate(currentTime())
                        .used(true)
                        .build();
                    return insertMemory(memory).get();
                })
                .map(memory -> {
                    // 일정 - 작성자 연결
                    memory.getWriter().addMemory(memory);
                    memory.addUser(memory.getWriter());
                    
                    // 일정 - 참여자 연결
                    addMemberToMemory(memory, request.getMembers());
                    addRoomToMemory(memory, request.getShareRooms());
                    Long roomId = relationMainRoom(memory, request);
                    
                    return new InsertMemoryResponseDto(memory.getId(), roomId, currentDate());
                })
                .orElseThrow(() -> new MemoryNotFoundWriterException(
                        "Not found writer matched to userId: " + request.getUserId()));
    }
    
    @Transactional
    public void addRoomToMemory(Memory memory, List<Long> roomIds) throws MemoryDataRelationException {
        Optional.ofNullable(roomIds).map(List::stream).ifPresent(stream -> stream.forEach(id -> {
            findRoom(id)
            .filter(Objects::nonNull)
            .map(room -> {
                room.addMemory(memory);
                memory.addRoom(room);

                room.getUsers().stream()
                        .forEach(user -> firebaseFcm.sendMessageTo(new FcmRequestDto(user.getPushToken(),
                                "OurMemory - 일정 공유", String.format("'%s' 일정이 방에 공유되었습니다.", memory.getName()))));
                return room;
            })
            .orElseThrow(() -> new MemoryDataRelationException(
                    String.format("Failed to relation the memory '%s' with the room '%s'", memory.getName(), id)));
        }));
    }
    
    @Transactional
    public void addMemberToMemory(Memory memory, List<Long> members)
            throws MemoryDataRelationException, UserNotFoundException {
        Optional.ofNullable(members).map(List::stream).ifPresent(stream -> stream.forEach(id -> {
            findUser(id).filter(Objects::nonNull).map(user -> {
                user.addMemory(memory);
                memory.addUser(user);

                firebaseFcm.sendMessageTo(new FcmRequestDto(user.getPushToken(), "OurMemory - 일정 공유",
                        String.format("'%s' 일정에 참여되셨습니다.", memory.getName())));
                return user;
            })
            .orElseThrow(() -> new MemoryDataRelationException(
                    String.format("Failed to relation the memory '%s' with the memberId '%d'", memory.getName(), id)));
        }));
    }
    
    /**
     * 일정이 포함될 메인 방 설정
     * 
     * 1. 메인방이 있는 경우
     *  1) 참여자가 없거나 방에 전부 포함되는 경우 -> 방 생성X, 일정-방 연결
     *  2) 참여자가 방에 포함되지 않는 경우 -> 방 생성 및 푸시알림, 일정-방 연결
     * 
     * 2. 메인방이 없는 경우
     *  1) 참여자가 있는 경우 -> 방 생성 및 푸시알림, 일정-방 연결
     *  2) 참여자가 없는 경우 -> 일정-사용자만 연결, 개인 일정으로 취급함.
     * 
     * @param memory
     * @param request 
     */
    private Long relationMainRoom(Memory memory, InsertMemoryRequestDto request) throws MemoryNotFoundRoomException {
        Room mainRoom = findRoom(request.getRoomId())
                // 1. 메인방이 있는 경우 -> 참여자가 전부 포함되는지 확인
                .filter(room -> {
                    List<Long> memoryMembers = memory.getUsers().stream().map(User::getId).collect(Collectors.toList());

                    return room.getUsers().stream().map(User::getId).collect(Collectors.toList())
                            .containsAll(memoryMembers);
                })
                // 1-2) 참여자가 메인방에 포함되지 않는 경우, 2. 메인방이 없는 경우
                .orElseGet(() -> Optional.ofNullable(request.getMembers())
                        // 1) 참여자가 있는 경우 -> 방 생성 후 푸시알림
                        .map(members -> {
                            // 프로토콜 작성
                            List<User> users = memory.getUsers();
                            String name = StringUtils
                                    .join(users.stream().map(User::getName).collect(Collectors.toList()), ", ");
                            Long owner = memory.getWriter().getId();
                            InsertRoomRequestDto insertRoomRequestDto = new InsertRoomRequestDto(name, owner, false,
                                    request.getMembers());
                            
                            // 방 생성
                            InsertRoomResponseDto insertRoomResponseDto = roomService.insert(insertRoomRequestDto);

                            // 푸시 알림
                            return findRoom(insertRoomResponseDto.getRoomId()).map(room -> {
                                room.getUsers().stream().forEach(
                                        user -> firebaseFcm.sendMessageTo(new FcmRequestDto(user.getPushToken(),
                                                "OurMemory - 방 생성", String.format("일정 '%s' 을 공유하기 위한 방 '%s' 가 생성되었습니다.",
                                                        memory.getName(), room.getName()))));
                                return room;
                            }).orElseThrow(() -> new MemoryNotFoundRoomException(
                                    String.format("Unable to find a room to include the memory '%s'. roomId: %d",
                                            memory.getName(), insertRoomResponseDto.getRoomId())));
                        })
                        // 참여자가 없는 경우
                        .orElse(null));

        return Optional.ofNullable(mainRoom)
                // 일정과 연결할 방이 있는 경우
                .map(room -> {
                    addRoomToMemory(memory, Arrays.asList(room.getId()));
                    return room.getId();
                }).orElse(null);
    }
    
    public List<Memory> findMemorys(Long userId) throws MemoryNotFoundWriterException {
        return findUser(userId)
                .map(User::getMemorys)
                .orElseThrow(() -> new MemoryNotFoundWriterException("Not found writer from userId: " + userId));
    }
    
    @Transactional
    public DeleteMemoryResponseDto deleteMemory(Long id) throws MemoryNotFoundException {
        return findMemory(id)
                .map(memory -> {
                    memory.getRooms().stream().forEach(room -> room.getMemorys().remove(memory));
                    memory.getUsers().stream().forEach(user -> user.getMemorys().remove(memory));
                    deleteMemory(memory);
                    return new DeleteMemoryResponseDto(currentDate());
                })
                .orElseThrow(() -> new MemoryNotFoundException("Not found memory matched to memoryid: " + id));
    }
    
    /**
     * Memory Repository 
     */
    private Optional<Memory> insertMemory(Memory memory) throws MemoryInternalServerException {
        return Optional.ofNullable(memoryRepo.save(memory))
                .map(Optional::of)
                .orElseThrow(() -> new MemoryInternalServerException(
                        String.format("Memory '%s' insert failed.", memory.getName())));
    }
    
    private Optional<Memory> findMemory(Long id) {
        return memoryRepo.findById(id);
    }
    
    private void deleteMemory(Memory memory) {
        memoryRepo.delete(memory);
    }
    
    /**
     * User Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return userRepo.findById(id);
    }
    
    /**
     * Room Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<Room> findRoom(Long id) {
        return roomRepo.findById(id);
    }
}
