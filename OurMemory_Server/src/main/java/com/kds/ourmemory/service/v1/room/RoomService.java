package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static com.kds.ourmemory.util.DateUtil.currentTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomResponseDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomRequestDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomResponseDto;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FcmService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final UserRepository userRepo;
    private final RoomRepository roomRepo;
    
    private final FcmService fcmService;

    @Transactional
    public InsertRoomResponseDto insert(InsertRoomRequestDto request) throws RoomInternalServerException {
        return Optional.ofNullable(request.getOwner())
            .map(ownerId -> findUserById(ownerId).get())
            .map(owner -> {
                Room room = Room.builder()
                    .owner(owner)
                    .name(request.getName())
                    .regDate(currentTime())
                    .opened(request.isOpened())
                    .used(true)
                    .users(new ArrayList<>())
                    .build();
                return insertRoom(room).get();
            })
            .map(room -> Optional.ofNullable(room.getOwner())
                    .map(owner -> owner.addRoom(room))
                    .map(room::addUser)
                    .map(r -> addMemberToRoom(r, request.getMember()))
                    .orElseThrow(() -> new RoomInternalServerException("Insert failed Relational Data to users_rooms."))
            )
            .map(room -> new InsertRoomResponseDto(room.getId(), currentDate()))
            .orElseThrow(() -> new RoomInternalServerException("Create Room Failed."));
    }

    @Transactional
    public Room addMemberToRoom(Room room, List<Long> members) throws RoomInternalServerException {
        Optional.ofNullable(members).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> 
                findUserById(id).filter(Objects::nonNull)
                .map(user -> {
                    user.addRoom(room);
                    room.addUser(user);
                    
                    fcmService.sendMessageTo(new FcmRequestDto(user.getPushToken(), "OurMemory - 방 참여",
                                    String.format("'%s' 방에 초대되셨습니다.", room.getName())));
                    return user;
                 })
                 .orElseThrow(() -> new RoomInternalServerException("memberId is Not Registered DB. id: " + id))
             ));
        
        return room;
    }
    
    public List<Room> findRooms(String snsId) throws UserNotFoundException {
        return findUserBySnsId(snsId).map(User::getRooms)
                .orElseThrow(() -> new UserNotFoundException("Not Found User From snsId: " + snsId));
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
    public DeleteRoomResponseDto delete(Long id) throws RoomInternalServerException {
        return findRoomById(id)
                .map(room -> {
                    room.getUsers().stream().forEach(user -> user.getRooms().remove(room));
                    room.getMemorys().stream().forEach(memory -> memory.getRooms().remove(room));
                    
                    deleteRoom(room);
                    return new DeleteRoomResponseDto(currentDate());
                })
                .orElseThrow(() -> new RoomInternalServerException("Delete Failed: " + id));
    }
    
    private Optional<Room> insertRoom(Room room) {
        return Optional.of(roomRepo.save(room));
    }
    
    private Optional<Room> findRoomById(Long id) {
        return roomRepo.findById(id);
    }
    
    private void deleteRoom(Room room) {
        roomRepo.delete(room);
    }
    
    private Optional<User> findUserById(Long id) {
        return userRepo.findById(id);
    }
    
    private Optional<User> findUserBySnsId(String snsId) {
        return userRepo.findBySnsId(snsId);
    }
}
