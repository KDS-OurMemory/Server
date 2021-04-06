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
    private final RoomService roomService;
    
    private final UserRepository userRepo;
    private final MemoryRepository memoryRepo;
    private final RoomRepository roomRepo;
    
    private final FcmService firebaseFcm;
    
    @Transactional
    public InsertMemoryResponseDto insert(InsertMemoryRequestDto request)
            throws MemoryNotFoundWriterException, MemoryDataRelationException, MemoryInternalServerException {
        return Optional.ofNullable(request)
                .map(req -> {
                    return findUserBySnsId(req.getSnsId())
                            .orElseThrow(() -> new MemoryNotFoundWriterException("Not found user matched to snsId: " + req.getSnsId()));
                })
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
                    return insert(memory)
                            .orElseThrow(() -> new MemoryInternalServerException("Add Memory to DB Failed."));
                })
                .map(memory -> {
                    // 일정 - 작성자 연결
                    Optional.ofNullable(memory.getWriter())
                        .map(writer -> writer.addMemory(memory))
                        .map(memory::addUser)
                        .orElseThrow(() -> new MemoryDataRelationException(
                                String.format("Failed to relation the memory '%s' with the writer '%s'",
                                        memory.getName(), memory.getWriter().getName())));
                    
                    // 일정 - 참여자 연결
                    addMemberToMemory(memory, request.getMembers());
                    addRoomToMemory(memory, request.getShareRooms());
                    Long roomId = relationMainRoom(memory, request);
                    
                    return new InsertMemoryResponseDto(memory.getId(), roomId, currentDate());
                })
                .orElseThrow(() -> new MemoryInternalServerException("Add Memory to DB Failed."));
    }
    
    @Transactional
    public void addRoomToMemory(Memory memory, List<Long> roomIds) throws MemoryDataRelationException {
        Optional.ofNullable(roomIds).map(List::stream).ifPresent(stream -> stream.forEach(id -> {
            findRoomById(id).filter(Objects::nonNull).map(room -> {
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
    public void addMemberToMemory(Memory memory, List<Long> members)throws MemoryDataRelationException {
        Optional.ofNullable(members).map(List::stream).ifPresent(stream -> stream.forEach(id -> {
            findUserById(id).filter(Objects::nonNull).map(user -> {
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
     * 1. 참여자가 방에 포함되지 않는 경우: 방을 새로 생성 -> 일정-방 연결
     * 2. 참여자가 방에 포함되는 경우: 방-일정 연결
     * 3. 참여자와 방이 없는 경우: 일정-사용자만 연결, 개인 일정으로 취급함.
     * 
     * @param memory
     * @param request
     */
    private Long relationMainRoom(Memory memory, InsertMemoryRequestDto request) throws MemoryInternalServerException{
        Room mainRoom = Optional.ofNullable(request.getRoomId())
            .map(id -> roomRepo.findById(id).get())
            .filter(room -> {
                List<Long> memoryMembers = memory.getUsers().stream().map(User::getId).collect(Collectors.toList());
                
                return room.getUsers().stream()
                    .map(User::getId).collect(Collectors.toList())
                    .containsAll(memoryMembers);
            })
            .orElseGet(() -> Optional.ofNullable(request.getMembers())
                .map(members -> {
                    List<User> users = memory.getUsers();
                    String name = StringUtils.join(users.stream().map(User::getName).collect(Collectors.toList()), ", ");
                    Long owner = memory.getWriter().getId();
                    InsertRoomRequestDto insertRoomRequestDto = new InsertRoomRequestDto(name, owner, false, request.getMembers());
                    InsertRoomResponseDto insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
                    
                    return roomRepo.findById(insertRoomResponseDto.getRoomId())
                        .map(room -> {
                            room.getUsers().stream()
                            .forEach(user -> firebaseFcm.sendMessageTo(
                                            new FcmRequestDto(user.getPushToken(), "OurMemory - 방 생성",
                                                    String.format("일정 '%s' 을 공유하기 위한 방 '%s' 가 생성되었습니다.",
                                                            memory.getName(), room.getName())))
                            );
                            return room;
                        })
                        .orElseThrow(() -> new MemoryInternalServerException("Insert memory OK. But Failed to create room to include memory."));
                })
                .orElse(null));
        
        return Optional.ofNullable(mainRoom)
            .map(room -> {
                addRoomToMemory(memory, Arrays.asList(room.getId()));
                return room.getId();
                })
            .orElse(null);
    }
    
    public List<Memory> findMemorys(String snsId) throws UserNotFoundException {
        return findUserBySnsId(snsId).map(User::getMemorys)
                .orElseThrow(() -> new UserNotFoundException("Not Found User From snsId: " + snsId));
    }
    
    @Transactional
    public DeleteMemoryResponseDto delete(Long id) throws MemoryInternalServerException {
        return findMemoryById(id)
                .map(memory -> {
                    memory.getRooms().stream().forEach(room -> room.getMemorys().remove(memory));
                    memory.getUsers().stream().forEach(user -> user.getMemorys().remove(memory));
                    delete(memory);
                    return new DeleteMemoryResponseDto(currentDate());
                })
                .orElseThrow(() -> new MemoryInternalServerException("Delete Failed: " + id));
    }
    
    private Optional<Memory> insert(Memory memory) {
        return Optional.of(memoryRepo.save(memory));
    }
    
    private Optional<Memory> findMemoryById(Long id) {
        return memoryRepo.findById(id);
    }
    
    private void delete(Memory memory) {
        memoryRepo.delete(memory);
    }
    
    private Optional<User> findUserById(Long id) {
        return userRepo.findById(id);
    }
    
    private Optional<User> findUserBySnsId(String snsId) {
        return userRepo.findBySnsId(snsId);
    }
    
    private Optional<Room> findRoomById(Long id) {
        return roomRepo.findById(id);
    }
}
