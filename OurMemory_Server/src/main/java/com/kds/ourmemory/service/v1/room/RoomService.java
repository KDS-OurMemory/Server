package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final UserRepository userRepo;
    private final RoomRepository roomRepo;
    
    private final FirebaseCloudMessageService firebaseFcm;

    @Transactional
    public InsertRoomResponseDto insert(InsertRoomRequestDto request) throws CRoomException {
        return Optional.ofNullable(request.getOwner())
        .map(ownerId -> userRepo.findById(ownerId).get())
        .map(user -> {
            Room room = Room.builder()
                .user(user)
                .name(request.getName())
                .regDate(new Date())
                .opened(request.isOpened())
                .used(true)
                .users(new ArrayList<>())
                .build();
            return roomRepo.save(room);
        })
        .map(room -> {
            User owner = room.getUser();
            owner.addRoom(room);
            room.addUser(owner);
            
            return addMemberToRoom(room, request.getMember());
        })
        .map(room -> new InsertRoomResponseDto(room.getId(), currentDate()))
        .orElseThrow(() -> new CRoomException("Create Room Failed."));
    }

    @Transactional
    public Room addMemberToRoom(Room room, List<Long> members) throws CRoomException {
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
        
        return room;
    }
    
    public List<Room> findRooms(String snsId) throws CUserNotFoundException {
        return userRepo.findBySnsId(snsId).map(User::getRooms)
                .orElseThrow(() -> new CUserNotFoundException("Not Found User From snsId: " + snsId));
    }
    
    /**
     * Transactional 하는 이유
     * 
     * 관계 데이터를 Lazy 타입으로 설정하였기 때문에 지연로딩이 발생하고, 지연로딩된 데이터는 영속성 컨텍스트 범위 내에서만 살아있다.
     * 해당 로직에 영속성 컨텍스트를 설정하기 위해 Transactional 처리하였다.
     * 자세한 내용은 아래 링크 참고.
     * https://doublesprogramming.tistory.com/259
     */
    @Transactional
    public DeleteRoomResponseDto delete(Long id) throws CRoomException {
        return roomRepo.findById(id)
                .map(room -> {
                    room.getUsers().stream().forEach(user -> user.getRooms().remove(room));
                    room.getMemorys().stream().forEach(memory -> memory.getRooms().remove(room));
                    
                    roomRepo.delete(room);
                    return new DeleteRoomResponseDto(currentDate());
                })
                .orElseThrow(() -> new CRoomException("Delete Failed: " + id));
    }
}
