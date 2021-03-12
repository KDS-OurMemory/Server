package com.kds.ourmemory.service.v1.room;

import java.text.SimpleDateFormat;
import java.util.List;
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
            int resultCode = Boolean.TRUE.equals(isAdd) ? 0 : 1;
            return new RoomResponseDto(resultCode, currentDate);
        }).orElseThrow(() -> new CRoomsException("addMemberToRoom Failed."));
    }

    public List<Rooms> findAll() {
        return roomRepo.findAll();
    }

    public Optional<Rooms> insert(Rooms room) {
        return Optional.of(roomRepo.save(room));
    }

    @Transactional
    public boolean addMemberToRoom(List<Long> members, Rooms room) throws CRoomsException {
        members.stream().forEach(id -> {
            userRepo.findById(id).map(user -> {
               user.addRoom(room);
               room.addUser(user);
               return true;
            })
            .orElseThrow(CRoomsException::new);
        });
        
        return true;
    }
}
