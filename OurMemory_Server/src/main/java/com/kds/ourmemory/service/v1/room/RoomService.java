package com.kds.ourmemory.service.v1.room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.domain.Rooms;
import com.kds.ourmemory.domain.Users;
import com.kds.ourmemory.domain.UsersAndRooms;
import com.kds.ourmemory.dto.room.RoomResponseDto;
import com.kds.ourmemory.repository.UsersAndRoomsRepository;
import com.kds.ourmemory.repository.room.RoomRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final UsersAndRoomsRepository usersAndRoomsRepository;
    
    public RoomResponseDto createRoom(Rooms room, Long[] member) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String today = format.format(new Date());

        Rooms saveRoom = roomRepository.save(room);
        
        Consumer<Long[]> addMemberToUsersAndRooms = memberIds -> Optional.ofNullable(memberIds).map(Arrays::stream)
                .ifPresent(m -> m.forEach(id -> usersAndRoomsRepository
                        .save(new UsersAndRooms(Users.builder().id(id).build(), saveRoom))));

        IntFunction<RoomResponseDto> response = code -> new RoomResponseDto(code, today);

        Optional.ofNullable(saveRoom).ifPresent(r -> addMemberToUsersAndRooms.accept(member));

        return Optional.ofNullable(saveRoom).map(r -> response.apply(0)).orElseGet(() -> response.apply(1));
    }
    
    public List<Rooms> findAll() {
        return roomRepository.findAll();
    }
    
    public Optional<Rooms> insert(Rooms room) {
        return Optional.of(roomRepository.save(room));
    }
}
