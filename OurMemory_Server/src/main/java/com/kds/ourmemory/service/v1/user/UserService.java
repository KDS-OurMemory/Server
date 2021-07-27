package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.*;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepo;

    private final RoomService roomService;

    private static final String NOT_FOUND_MESSAGE = "Not found '%s' matched id: %d";
    private static final String NOT_FOUND_LOGIN_USER_MESSAGE = "Not found user matched snsType '%d' and snsId '%s'.";

    @Transactional
    public InsertUserDto.Response signUp(InsertUserDto.Request request) {
        var user = request.toEntity();
        return insertUser(user).map(u -> new InsertUserDto.Response(u.getId(), u.formatRegDate()))
                .orElseThrow(() -> new UserInternalServerException(
                                String.format("User '%s' insert failed.", user.getName())
                        )
                );
    }

    public SignInUserDto.Response signIn(int snsType, String snsId) {
        return findUser(snsType, snsId).map(SignInUserDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(
                                String.format(NOT_FOUND_LOGIN_USER_MESSAGE, snsType, snsId)
                        )
                );
    }

    public FindUserDto.Response find(long userId) {
        return findUser(userId)
                .map(FindUserDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(
                            String.format(NOT_FOUND_MESSAGE, "user", userId)
                        )
                );
    }

    public List<FindUsersDto.Response> findUsers(Long userId, String name) {
        return findUsersByIdOrName(userId, name)
                .map(users -> users.stream().map(FindUsersDto.Response::new)
                        .collect(Collectors.toList())
                )
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found users matched id '%d' or name '%s'", userId, name)));
    }

    @Transactional
    public PatchTokenDto.Response patchToken(long userId, PatchTokenDto.Request request) {
        return findUser(userId).map(user ->
                user.changePushToken(request.getPushToken())
                        .map(u -> new PatchTokenDto.Response(u.formatModDate()))
                        .orElseThrow(() -> new UserInternalServerException("Failed to patch for user token."))
                )
                .orElseThrow(() -> new UserNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, "user", userId)
                        )
                );
    }

    @Transactional
    public UpdateUserDto.Response update(long userId, UpdateUserDto.Request request) {
        return findUser(userId).map(user ->
                user.updateUser(request)
                        .map(u -> new UpdateUserDto.Response(u.formatModDate()))
                        .orElseThrow(() -> new UserInternalServerException("Failed to update for user data."))
                )
                .orElseThrow(() -> new UserNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, "user", userId)
                        )
                );
    }

    /**
     * Delete user
     *
     * user - used = false
     *
     * Related memories
     *  1) owner, member - maintain
     *  2) private - Delete memories
     *
     * Related rooms
     *  1) owner - transferring the owner and delete user from room member.
     *  2) participant - Delete user from room member
     *  3) private - Delete room
     *
     * @param userId [long]
     * @return DeleteUserDto.Response
     */
    public DeleteUserDto.Response delete(long userId) {
        return findUser(userId)
                .map(User::deleteUser)
                .map(user -> {
                    user.getRooms().forEach(room -> {
                        var members = room.getUsers();

                        if (members.size() > 1) {
                            var owner = room.getOwner();
                            // Related rooms - 1) owner room
                            if (owner.equals(user)) {
                                transferOwner(room.getId(), owner.getId(), members);
                            }

                            // Related rooms - 1) 2)
                            room.deleteUser(user);
                        }
                        // Related rooms - 3)
                        else if (members.size() == 1) {
                            roomService.delete(room.getId());
                        }
                    });
                    user.deleteRooms(user.getRooms());

                    return user;
                })
                .map(user -> {
                    // Related memories - 2)
                    user.getMemories().forEach(memory ->
                        Optional.of(memory).filter(m -> m.getRooms().isEmpty())
                                .ifPresent(Memory::deleteMemory)
                    );
                    return user;
                })
                .map(user -> new DeleteUserDto.Response(user.formatModDate()))
                .orElseThrow(() -> new UserNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, "user", userId)
                        )
                );
    }

    private void transferOwner(long roomId, long ownerId, List<User> users) {
        var transferIds = users.stream().map(User::getId).filter(id -> id != ownerId)
                .collect(Collectors.toList());
        Optional.of(transferIds)
                .filter(ids -> !ids.isEmpty())
                .map(ids -> ids.get(0))
                .ifPresent(id -> roomService.patchOwner(roomId, id));
    }

    /**
     * User Repository
     */
    private Optional<User> insertUser(User user) {
        return Optional.of(userRepo.save(user));
    }

    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepo::findById);
    }

    private Optional<User> findUser(int snsType, String snsId) {
        return Optional.ofNullable(snsId).flatMap(sid -> userRepo.findBySnsIdAndSnsType(snsId, snsType));
    }

    private Optional<List<User>> findUsersByIdOrName(Long userId, String name) {
        return Optional.ofNullable(userRepo.findAllByIdOrName(userId, name))
                .filter(users -> users.isPresent() && !users.get().isEmpty())
                .orElseGet(Optional::empty);
    }
}
