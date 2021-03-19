package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.room.dto.DeleteResponseDto;
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
                        .map(user -> user.addRoom(r))
                        .map(r::addUser).get())
                .map(r -> addMemberToRoom(r, members))
                .map(r -> new InsertResponseDto(r.getId(), currentDate()))
                .orElseThrow(() -> new CRoomException("Create Room Failed."));
    }
    
    private Optional<Room> insert(Room room) {
        return Optional.of(roomRepo.save(room));
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
    public DeleteResponseDto delete(Long roomId) throws CRoomException {
        return roomRepo.findById(roomId)
                .map(room -> {
                    room.getUsers().stream().forEach(user -> user.getRooms().remove(room));
                    
                    roomRepo.delete(room);
                    return new DeleteResponseDto(currentDate());
                }).orElseThrow(() -> new CRoomException("Delete Failed: " + roomId));
    }
}
