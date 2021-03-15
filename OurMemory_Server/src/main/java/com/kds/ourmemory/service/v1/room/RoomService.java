package com.kds.ourmemory.service.v1.room;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CRoomsException;
import com.kds.ourmemory.controller.v1.room.dto.RoomResponseDto;
import com.kds.ourmemory.entity.room.Rooms;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    @Transactional
    public RoomResponseDto createRoom(Rooms room, List<Long> members) throws CRoomsException {
        return insert(room).map(r -> addMemberToRoom(members, r)).map(isAdd -> {
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
            return new RoomResponseDto(currentDate);
        }).orElseThrow(() -> new CRoomsException("Create Room Failed."));
    }

    @Transactional
    public boolean addMemberToRoom(List<Long> members, Rooms room) throws CRoomsException {
        Optional.ofNullable(members).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> {
                userRepo.findById(id).filter(Objects::nonNull)
                .map(user -> {
                    user.addRoom(room);
                    room.addUser(user);
                    return user;
                 })
                 .orElseThrow(() -> new CRoomsException("memberId is Not Registered DB. id: " + id));
             }));
        
        return true;
    }
    
    public Optional<Rooms> insert(Rooms room) {
        return Optional.of(roomRepo.save(room));
    }
}
