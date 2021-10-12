package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserProfileImageUploadException;
import com.kds.ourmemory.config.S3Uploader;
import com.kds.ourmemory.controller.v1.room.dto.DeleteRoomDto;
import com.kds.ourmemory.controller.v1.user.dto.*;
import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.friend.FriendRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    // When delete a user, deleted because sometimes a room is deleted or transfer owner
    private final RoomService roomService;

    // For profile upload
    private final S3Uploader s3Uploader;

    // When searching for a user, add to pass the friend status
    private final FriendRepository friendRepository;

    private static final String NOT_FOUND_MESSAGE = "Not found '%s' matched id: %d";

    private static final String NOT_FOUND_LOGIN_USER_MESSAGE = "Not found user matched snsType '%d' and snsId '%s'.";

    private static final String FAILED_MESSAGE = "Failed to %s for %s";

    private static final String USER = "user";

    private static final String INSERT = "insert";

    private static final String PATCH = "patch";

    private static final String UPDATE = "update";

    private static final String PROFILE_IMAGE_DIR = "profileImages";

    @Transactional
    public InsertUserDto.Response signUp(InsertUserDto.Request request) {
        var user = request.toEntity();
        return insertUser(user)
                .map(u -> {
                    var privateRoomId = roomService.insertPrivateRoom(u.getId());
                    user.updatePrivateRoomId(privateRoomId);

                    return new InsertUserDto.Response(u.getId(), privateRoomId);
                })
                .orElseThrow(() -> new UserInternalServerException(
                                String.format(FAILED_MESSAGE, INSERT, USER + user.getName())
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
                            String.format(NOT_FOUND_MESSAGE, USER, userId)
                        )
                );
    }

    public List<FindUsersDto.Response> findUsers(long userId, Long findId, String name, FriendStatus friendStatus) {
        // Find by friendStatus
        var responseList = findFriendsByUserId(userId)
                .map(list -> list.stream().filter(friend -> friend.getStatus().equals(friendStatus))
                        .map(friend -> new FindUsersDto.Response(friend.getFriendUser(), friend))
                        .collect(Collectors.toList())
                )
                .orElseGet(ArrayList::new);

        // Find by findId or name
        responseList.addAll(
                findUsersByIdOrName(findId, name)
                        .map(users -> users.stream().map(user -> {
                                            Friend friend = findFriend(userId, findId)
                                                    .orElse(null);
                                            return new FindUsersDto.Response(user, friend);
                                        })
                                        .collect(Collectors.toList())
                        )
                        .orElseGet(ArrayList::new)
        );

        return responseList.stream().distinct().collect(Collectors.toList());
    }

    @Transactional
    public PatchTokenDto.Response patchToken(long userId, PatchTokenDto.Request request) {
        return findUser(userId).map(user ->
                user.changePushToken(request.getPushToken())
                        .map(u -> new PatchTokenDto.Response())
                        .orElseThrow(() -> new UserInternalServerException(
                                String.format(FAILED_MESSAGE, PATCH, USER + " token")))
                )
                .orElseThrow(() -> new UserNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, USER, userId)
                        )
                );
    }

    @Transactional
    public UpdateUserDto.Response update(long userId, UpdateUserDto.Request request) {
        return findUser(userId).map(user ->
                user.updateUser(request)
                        .map(u -> new UpdateUserDto.Response())
                        .orElseThrow(() -> new UserInternalServerException(
                                String.format(FAILED_MESSAGE, UPDATE, USER + " data")))
                )
                .orElseThrow(() -> new UserNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, USER, userId)
                        )
                );
    }

    @Transactional
    public UploadProfileImageDto.Response uploadProfileImage(long userId, UploadProfileImageDto.Request request) {
        // 1. Check image
        checkNotNull(request.getProfileImage(), "ProfileImage is Null.");

        // 2. Get user
        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format(NOT_FOUND_MESSAGE, USER, userId)));

        // 3. Check and delete exists image
        if (Objects.nonNull(user.getProfileImageUrl())) {
            s3Uploader.delete(user.getProfileImageUrl());
        }

        // 4. Upload image
        return Optional.ofNullable(s3Uploader.upload(request.getProfileImage(), PROFILE_IMAGE_DIR))
                .map(url -> {
                    user.updateProfileImageUrl(url);
                    return new UploadProfileImageDto.Response(url);
                })
                .orElseThrow(() -> new UserProfileImageUploadException("Failed upload image."));
    }

    @Transactional
    public DeleteProfileImageDto.Response deleteProfileImage(long userId) {
        return findUser(userId)
                .map(user -> {
                    s3Uploader.delete(user.getProfileImageUrl());
                    user.updateProfileImageUrl(null);
                    return new DeleteProfileImageDto.Response();
                })
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(NOT_FOUND_MESSAGE, USER, userId)
                ));
    }

    /**
     * Delete user
     *
     * 1. friend - delete both side
     *
     * 2. Related rooms
     *  1) owner - transferring the owner and delete user from room member.
     *  2) participant - Delete user from room member
     *  3) private - Delete room -> delete private room's memories(side effect)
     *
     * 3.user - used = false
     *
     * @param userId [long]
     * @return DeleteUserDto.Response
     */
    @Transactional
    public DeleteUserDto.Response delete(long userId) {
        return findUser(userId)
                .map(user -> {
                    findFriendsByUserOrFriendUser(user, user)
                            .ifPresent(friends -> friends.forEach(this::deleteFriend));

                    return user;
                })
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
                        if (room.getId().longValue() == user.getPrivateRoomId()) {
                            roomService.delete(user.getPrivateRoomId(), new DeleteRoomDto.Request(user.getId()));
                        }
                    });
                    user.deleteRooms(user.getRooms());

                    return user;
                })
                // Delete the user after all job.
                .map(user -> {
                    user.deleteUser();
                    return new DeleteUserDto.Response();
                })
                .orElseThrow(() -> new UserNotFoundException(
                                String.format(NOT_FOUND_MESSAGE, USER, userId)
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
        return Optional.of(userRepository.save(user));
    }

    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepository::findById).filter(User::isUsed);
    }

    private Optional<User> findUser(int snsType, String snsId) {
        return Optional.ofNullable(snsId).flatMap(
                sid -> userRepository.findByUsedAndSnsIdAndSnsType(true, snsId, snsType)
        );
    }

    private Optional<List<User>> findUsersByIdOrName(Long userId, String name) {
        return Optional.ofNullable(userRepository.findAllByUsedAndIdOrName(true, userId, name))
                .filter(users -> users.isPresent() && !users.get().isEmpty())
                .orElseGet(Optional::empty);
    }

    /**
     * Friend Repository
     *
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<Friend> findFriend(Long userId, Long friendId) {
        return Optional.ofNullable(userId)
                .flatMap(u -> friendRepository.findByUserIdAndFriendUserId(userId, friendId));
    }

    private Optional<List<Friend>> findFriendsByUserId(Long userId) {
        return Optional.ofNullable(userId)
                .flatMap(friendRepository::findAllByUserId);
    }

    private Optional<List<Friend>> findFriendsByUserOrFriendUser(User user, User friendUser) {
        return Optional.ofNullable(user)
                .flatMap(u -> friendRepository.findAllByUserOrFriendUser(user, friendUser));
    }

    private void deleteFriend(Friend friend) {
        friendRepository.delete(friend);
    }
}
