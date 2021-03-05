package com.kds.ourmemory.service.v1.room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CUsersAndRoomsException;
import com.kds.ourmemory.domain.UsersAndRooms;
import com.kds.ourmemory.domain.room.Rooms;
import com.kds.ourmemory.dto.room.RoomResponseDto;
import com.kds.ourmemory.repository.UsersAndRoomsRepository;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    private final UsersAndRoomsRepository usersAndRoomsRepo;
    
    @Transactional
    public RoomResponseDto createRoom(Rooms room, List<Long> members) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String today = format.format(new Date());

        IntFunction<RoomResponseDto> response = code -> new RoomResponseDto(code, today);
        
        return insert(room)
            .map(r -> addMemberToRoom(members, r))
            .map(b -> response.apply(0))
            .orElseThrow(() -> new CUsersAndRoomsException("addMemberToRoom Failed."));
    }
    
    public List<Rooms> findAll() {
        return roomRepo.findAll();
    }
    
    public Optional<Rooms> insert(Rooms room) {
        return Optional.of(roomRepo.save(room));
    }
    
    @Transactional
    public boolean addMemberToRoom(List<Long> members, Rooms room) throws CUsersAndRoomsException {
        Long count = members.stream()
                .map(id -> userRepo.findById(id).map(u -> usersAndRoomsRepo.save(new UsersAndRooms(u, room)))).count();
        return count == members.size();
    }
}
