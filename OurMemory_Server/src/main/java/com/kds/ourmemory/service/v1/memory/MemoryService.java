package com.kds.ourmemory.service.v1.memory;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryResponseDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryResponseDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.repository.memory.MemoryRepository;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemoryService {
    private final UserRepository userRepo;
    private final MemoryRepository memoryRepo;
    private final RoomRepository roomRepo;
    
    private final FirebaseCloudMessageService firebaseFcm;
    
    @Transactional
    public InsertMemoryResponseDto insert(InsertMemoryRequestDto request) throws CMemoryException{
        return userRepo.findBySnsId(request.getSnsId())
                .map(user -> {
                    Memory memory = Memory.builder()
                        .user(user)
                        .name(request.getName())
                        .contents(request.getContents())
                        .place(request.getPlace())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .firstAlarm(request.getFirstAlarm())
                        .secondAlarm(request.getSecondAlarm())
                        .bgColor(request.getBgColor())
                        .regDate(new Date())
                        .used(true)
                        .build();
                    return memoryRepo.save(memory);
                })
                .map(memory -> {
                    // 사용자 - 일정 연결
                    Optional.ofNullable(memory.getUser())
                        .map(writer -> writer.addMemory(memory))
                        .map(memory::addUser)
                        .orElseThrow(() -> new CMemoryException("Insert failed Relational Data to users_memorys."));
                    addMemberToMemory(memory, request.getMembers());
                    
                    // 일정 - 방 연결
                    roomRepo.findById(request.getRoomId())
                        .map(room -> room.addMemory(memory))
                        .map(memory::addRoom)
                        .orElseThrow(() -> new CMemoryException("Insert failed Relational Data to memorys_rooms."));
                    addRoomToMemory(memory, request.getRoomIds());
                    
                    return memory;
                })
                .map(memory -> new InsertMemoryResponseDto(memory.getId(), currentDate()))
                .orElseThrow(() -> new CMemoryException("Add Memory to DB Failed."));
    }
    
    @Transactional
    public Memory addRoomToMemory(Memory memory, List<Long> roomIds)throws CMemoryException {
        Optional.ofNullable(roomIds).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> {
                roomRepo.findById(id).filter(Objects::nonNull)
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
    public Memory addMemberToMemory(Memory memory, List<Long> members)throws CMemoryException {
        Optional.ofNullable(members).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> {
                userRepo.findById(id).filter(Objects::nonNull)
                .map(user -> {
                    user.addMemory(memory);
                    memory.addUser(user);
                    
                    // 일정 참여자에게 푸시알림
                    firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - share Memory", "Share Memory " + memory.getName());
                    return user;
                 })
                 .orElseThrow(() -> new CRoomException("memberId is Not Registered DB. id: " + id));
             }));
    
        return memory;
    }
    
    @Transactional
    public DeleteMemoryResponseDto delete(Long id) throws CMemoryException {
        return memoryRepo.findById(id)
                .map(memory -> {
                    memory.getRooms().stream().forEach(room -> room.getMemorys().remove(memory));
                    memory.getUsers().stream().forEach(user -> user.getMemorys().remove(memory));
                    memoryRepo.delete(memory);
                    return new DeleteMemoryResponseDto(currentDate());
                })
                .orElseThrow(() -> new CMemoryException("Delete Failed: " + id));
    }
}
