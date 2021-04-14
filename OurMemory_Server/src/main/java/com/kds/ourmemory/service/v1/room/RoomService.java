package com.kds.ourmemory.service.v1.room;

import static com.kds.ourmemory.util.DateUtil.currentDate;
import static com.kds.ourmemory.util.DateUtil.currentTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.room.exception.RoomDataRelationException;
import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundOwnerException;
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
    private final RoomRepository roomRepo;
    
    // 방과 관련된 사용자 데이터를 작업하기 위해 추가 
    private final UserRepository userRepo;
    
    private final FcmService fcmService;

    @Transactional
    public InsertRoomResponseDto insert(InsertRoomRequestDto request)
            throws RoomDataRelationException, RoomNotFoundOwnerException, RoomInternalServerException {
        return findUser(request.getOwner())
                .map(owner -> {
                    Room room = Room.builder()
                        .owner(owner)
                        .name(request.getName())
                        .regDate(currentTime())
                        .opened(request.isOpened())
                        .used(true)
                        .users(new ArrayList<>())
                        .build();
                    return insertRoom(room)
                            .orElseThrow(() -> new RoomInternalServerException(String.format(
                                    "Insert room failed. [name: %s, owner: %s]", request.getName(), owner.getName())));
                })
                .map(room -> {
                    // 생성자 - 방 연결
                    room.getOwner().addRoom(room);
                    room.addUser(room.getOwner());
                    
                    // 참여자 - 방 연결
                    return addMemberToRoom(room, request.getMember());
                })
                .map(room -> new InsertRoomResponseDto(room.getId(), currentDate()))
                .orElseThrow(() -> new RoomNotFoundOwnerException(
                        "Not found user matched to userId: " + request.getOwner()));
    }

    @Transactional
    public Room addMemberToRoom(Room room, List<Long> members) throws RoomDataRelationException {
        Optional.ofNullable(members).map(List::stream)
            .ifPresent(stream -> stream.forEach(id -> 
                findUser(id)
                .map(user -> {
                    user.addRoom(room);
                    room.addUser(user);
                    
                    fcmService.sendMessageTo(new FcmRequestDto(user.getPushToken(), "OurMemory - 방 참여",
                                    String.format("'%s' 방에 초대되셨습니다.", room.getName())));
                    return user;
                 })
                 .orElseThrow(() -> new RoomNotFoundOwnerException("Not found member matched to userId: " + id))
             ));
        
        return room;
    }
    
    public List<Room> findRooms(Long userId) throws RoomNotFoundException {
        return findUser(userId).map(User::getRooms)
                .orElseThrow(() -> new RoomNotFoundException("Not Found rooms from user matched to userId: " + userId));
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
    public DeleteRoomResponseDto delete(Long id) throws RoomNotFoundException {
        return findRoom(id)
                .map(room -> {
                    Optional.ofNullable(room.getUsers())
                            .ifPresent(users -> users.stream().forEach(user -> user.getRooms().remove(room)));
                    
                    Optional.ofNullable(room.getMemorys())
                            .ifPresent(memorys -> memorys.stream().forEach(memory -> memory.getRooms().remove(room)));
                    
                    deleteRoom(room);
                    return new DeleteRoomResponseDto(currentDate());
                })
                .orElseThrow(() -> new RoomNotFoundException("Not found room matched roomId: " + id));
    }
    
    /**
     * Room Repository
     */
    private Optional<Room> insertRoom(Room room) {
        return Optional.ofNullable(roomRepo.save(room));
    }
    
    private Optional<Room> findRoom(Long id) {
        return Optional.ofNullable(id)
                .map(roomRepo::findById)
                .orElseGet(Optional::empty);
    }
    
    private void deleteRoom(Room room) {
        Optional.ofNullable(room)
            .ifPresent(roomRepo::delete);
    }
    
    /**
     * User Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id)
                .map(userRepo::findById)
                .orElseGet(Optional::empty);
    }
}
