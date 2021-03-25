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

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
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
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemoryService {
    private final RoomService roomService;
    
    private final UserRepository userRepo;
    private final MemoryRepository memoryRepo;
    private final RoomRepository roomRepo;
    
    private final FirebaseCloudMessageService firebaseFcm;
    
    @Transactional
    public InsertMemoryResponseDto insert(InsertMemoryRequestDto request) throws CMemoryException{
        return findUserBySnsId(request.getSnsId())
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
                    return insert(memory);
                })
                .map(memory -> {
                    // 사용자 - 일정 연결
                    Optional.ofNullable(memory.getWriter())
                        .map(writer -> writer.addMemory(memory))
                        .map(memory::addUser)
                        .orElseThrow(() -> new CMemoryException("Insert failed Relational Data to users_memorys."));
                    
                    addMemberToMemory(memory, request.getMembers());
                    addRoomToMemory(memory, request.getShareRooms());
                    relationMainRoom(memory, request);
                    return memory;
                })
                .map(memory -> new InsertMemoryResponseDto(memory.getId(), currentDate()))
                .orElseThrow(() -> new CMemoryException("Add Memory to DB Failed."));
    }
    
    @Transactional
    public Memory addRoomToMemory(Memory memory, List<Long> roomIds)throws CMemoryException {
        Optional.ofNullable(roomIds).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> {
                findRoomById(id).filter(Objects::nonNull)
                .map(room -> {
                    room.addMemory(memory);
                    memory.addRoom(room);
                    
                    // 방 참여자 모두에게 푸시알림
                    room.getUsers().stream().forEach(user -> firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - share Memory", "Share Memory " + memory.getName()));
                    return room;
                 })
                 .orElseThrow(() -> new CRoomException("memberId is Not Registered DB. id: " + id));
             }));
        
        return memory;
    }
    
    @Transactional
    public void addMemberToMemory(Memory memory, List<Long> members)throws CMemoryException {
        Optional.ofNullable(members).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> {
                findUserById(id).filter(Objects::nonNull)
                .map(user -> {
                    user.addMemory(memory);
                    memory.addUser(user);
                    
                    // 일정 참여자에게 푸시알림
                    firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - share Memory", "Share Memory " + memory.getName());
                    return user;
                 })
                 .orElseThrow(() -> new CRoomException("memberId is Not Registered DB. id: " + id));
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
    private void relationMainRoom(Memory memory, InsertMemoryRequestDto request) {
        Room mainRoom = Optional.ofNullable(request.getRoomId())
            .map(id -> roomRepo.findById(id).get())
            .filter(room -> {
                List<Long> memoryMembers = memory.getUsers().stream().map(User::getId).collect(Collectors.toList());
                
                return room.getUsers().stream()
                    .map(User::getId).collect(Collectors.toList())
                    .containsAll(memoryMembers);
            })
            .orElseGet(() -> Optional.ofNullable(memory.getUsers())
                                .map(users -> {
                                    String name = StringUtils.join(users.stream().map(User::getName).collect(Collectors.toList()), ", ");
                                    Long owner = memory.getWriter().getId();
                                    InsertRoomRequestDto insertRoomRequestDto = new InsertRoomRequestDto(name, owner, false, request.getMembers());
                                    InsertRoomResponseDto insertRoomResponseDto = roomService.insert(insertRoomRequestDto);
                                    return roomRepo.findById(insertRoomResponseDto.getId()).get();
                                })
                                .orElse(null));
        
        Optional.ofNullable(mainRoom)
            .map(room -> addRoomToMemory(memory, Arrays.asList(room.getId())));
    }
    
    public List<Memory> findMemorys(String snsId) throws CUserNotFoundException {
        return findUserBySnsId(snsId).map(User::getMemorys)
                .orElseThrow(() -> new CUserNotFoundException("Not Found User From snsId: " + snsId));
    }
    
    @Transactional
    public DeleteMemoryResponseDto delete(Long id) throws CMemoryException {
        return findMemoryById(id)
                .map(memory -> {
                    memory.getRooms().stream().forEach(room -> room.getMemorys().remove(memory));
                    memory.getUsers().stream().forEach(user -> user.getMemorys().remove(memory));
                    delete(memory);
                    return new DeleteMemoryResponseDto(currentDate());
                })
                .orElseThrow(() -> new CMemoryException("Delete Failed: " + id));
    }
    
    private Memory insert(Memory memory) {
        return memoryRepo.save(memory);
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
