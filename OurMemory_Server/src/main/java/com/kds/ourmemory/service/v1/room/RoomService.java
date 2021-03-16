package com.kds.ourmemory.service.v1.room;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.InsertResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    
    private final FirebaseCloudMessageService firebaseFcm;

    @Transactional
    public InsertResponseDto insert(Room room, List<Long> members) throws CRoomException {
        return insert(room)
                .map(r -> userRepo.findById(r.getOwner())
                        .map(user -> user.addRoom(r).get())
                        .map(r::addUser).get().get())
                .map(r -> addMemberToRoom(members, r)).map(isAdd -> {
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
            return new InsertResponseDto(currentDate);
        }).orElseThrow(() -> new CRoomException("Create Room Failed."));
    }

    @Transactional
    public boolean addMemberToRoom(List<Long> members, Room room) throws CRoomException {
        Optional.ofNullable(members).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> {
                userRepo.findById(id).filter(Objects::nonNull)
                .map(user -> {
                    user.addRoom(room);
                    room.addUser(user);
                    
                    firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - Invited Room", "Invited From " + room.getName());
                    return user;
                 })
                 .orElseThrow(() -> new CRoomException("memberId is Not Registered DB. id: " + id));
             }));
        
        return true;
    }
    
    private Optional<Room> insert(Room room) {
        return Optional.of(roomRepo.save(room));
    }
    
    public List<Room> findRooms(String snsId) throws CUserNotFoundException {
        return userRepo.findBySnsId(snsId).map(User::getRooms)
                .orElseThrow(() -> new CUserNotFoundException("Not Found User From snsId: " + snsId));
    }
}
