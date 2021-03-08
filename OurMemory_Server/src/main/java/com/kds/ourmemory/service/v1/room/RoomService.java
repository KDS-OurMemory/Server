package com.kds.ourmemory.service.v1.room;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CRoomsException;
import com.kds.ourmemory.domain.room.Rooms;
import com.kds.ourmemory.domain.user.Users;
import com.kds.ourmemory.dto.room.RoomResponseDto;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    
    @Transactional
    public RoomResponseDto createRoom(Rooms room, List<Long> members) {
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());

        IntFunction<RoomResponseDto> response = code -> new RoomResponseDto(code, currentDate);
        
        return insert(room)
            .map(r -> addMemberToRoom(members, r))
            .map(b -> response.apply(0))
            .orElseThrow(() -> new CRoomsException("addMemberToRoom Failed."));
    }
    
    public List<Rooms> findAll() {
        return roomRepo.findAll();
    }
    
    public Optional<Rooms> insert(Rooms room) {
        return Optional.of(roomRepo.save(room));
    }
    
    @Transactional
    public boolean addMemberToRoom(List<Long> members, Rooms room) throws CRoomsException {
        List<Users> users = new ArrayList<>();
        
        for (Long id : members) {
            Users user = userRepo.findById(id).filter(u -> u.getRooms()==null)
            .map(u -> u.setRooms(new ArrayList<>()).get())
            .orElseGet(()->userRepo.findById(id).get());
            
            users.add(user.addRoom(room).map(userRepo::save).get());
        }
        
        return room.addUsers(users).map(roomRepo::save).isPresent();
    }
}
