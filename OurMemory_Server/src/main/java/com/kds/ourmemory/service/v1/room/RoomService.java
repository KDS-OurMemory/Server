package com.kds.ourmemory.service.v1.room;

import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundMemberException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundOwnerException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.FindRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.InsertRoomDto;
import com.kds.ourmemory.controller.v1.room.dto.UpdateRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.room.RoomRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoomService {
    private final RoomRepository roomRepo;
    
    // Add to work in rooms and user relationship tables
    private final UserRepository userRepo;

    // Add to FCM
    private final FcmService fcmService;

    private static final String NOT_FOUND_MESSAGE = "Not found '%s' matched id: %d";

    @Transactional
    public InsertRoomDto.Response insert(InsertRoomDto.Request request) {
        return findUser(request.getOwner())
                .map(owner -> {
                    var room = Room.builder()
                            .owner(owner)
                            .name(request.getName())
                            .opened(request.isOpened())
                            .used(true)
                            .build();
                    return insertRoom(room)
                            .orElseThrow(() -> new RoomInternalServerException(String.format(
                                    "Insert room failed. [name: %s, owner: %s]", request.getName(), owner.getName())));
                })
                .map(room -> {
                    // Relation room and owner
                    room.getOwner().addRoom(room);
                    room.addUser(room.getOwner());

                    // Relation room and members
                    return addMemberToRoom(room, request.getMember());
                })
                .map(InsertRoomDto.Response::new)
                .orElseThrow(() -> new RoomNotFoundOwnerException(
                                String.format(NOT_FOUND_MESSAGE, "user", request.getOwner())
                        )
                );
    }

    private Room addMemberToRoom(Room room, List<Long> members) {
        Optional.ofNullable(members)
                .ifPresent(mem -> mem.forEach(id ->
                        findUser(id).map(user -> {
                                user.addRoom(room);
                                room.addUser(user);

                                fcmService.sendMessageTo(new FcmDto.Request(user.getPushToken(), user.getDeviceOs(),
                                        "OurMemory - 방 참여", String.format("'%s' 방에 초대되셨습니다.", room.getName())));
                                return user;
                        })
                        .orElseThrow(() -> new RoomNotFoundMemberException(
                                        String.format(NOT_FOUND_MESSAGE, "member", id)
                                )
                        )
                )
        );
        
        return room;
    }

    public FindRoomDto.Response find(Long roomId) {
        return findRoom(roomId)
                .filter(Room::isUsed)
                .map(FindRoomDto.Response::new)
                .orElseThrow(() -> new RoomNotFoundException(
                            String.format(NOT_FOUND_MESSAGE, "room", roomId)
                        )
                );
    }

    public List<Room> findRooms(Long userId, String name) {
        return findUser(userId).map(
                user -> findRoomsByOwnerOrName(user, name)
                        .map(List::stream)
                        .map(stream -> stream.filter(Room::isUsed).collect(Collectors.toList()))
                        .orElseGet(ArrayList::new)
        )
        .orElseThrow(
                () -> new RoomNotFoundOwnerException(String.format(NOT_FOUND_MESSAGE, "owner", userId))
        );
    }

    public UpdateRoomDto.Response update(long roomId, UpdateRoomDto.Request request) {
        return findRoom(roomId).map(room ->
                room.updateRoom(request)
                        .map(r -> new UpdateRoomDto.Response(r.formatModDate()))
                        .orElseThrow(() -> new RoomInternalServerException("Failed to update for room data."))
        )
        .orElseThrow(
                () -> new RoomNotFoundException(String.format(NOT_FOUND_MESSAGE, "room", roomId))
        );
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
    public DeleteRoomDto.Response delete(long id) {
        return findRoom(id)
                .map(Room::deleteRoom)
                .map(r -> new DeleteRoomDto.Response(BaseTimeEntity.formatNow()))
                .orElseThrow(() -> new RoomNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, "room", id)
                        )
                );
    }
    
    /**
     * Room Repository
     */
    private Optional<Room> insertRoom(Room room) {
        return Optional.of(roomRepo.save(room));
    }
    
    private Optional<Room> findRoom(Long id) {
        return Optional.ofNullable(id).flatMap(roomRepo::findById);
    }

    private Optional<List<Room>> findRoomsByOwnerOrName(User user, String name) {
        return roomRepo.findAllByOwnerOrName(user, name);
    }

    /**
     * User Repository
     * 
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepo::findById);
    }
}
