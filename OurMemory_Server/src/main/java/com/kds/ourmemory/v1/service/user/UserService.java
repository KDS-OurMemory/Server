package com.kds.ourmemory.v1.service.user;

import com.kds.ourmemory.v1.advice.user.exception.UserInternalServerException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotFoundException;
import com.kds.ourmemory.v1.advice.user.exception.UserProfileImageUploadException;
import com.kds.ourmemory.v1.util.S3Uploader;
import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import com.kds.ourmemory.v1.controller.user.dto.UserRspDto;
import com.kds.ourmemory.v1.entity.friend.Friend;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.repository.friend.FriendRepository;
import com.kds.ourmemory.v1.repository.user.UserRepository;
import com.kds.ourmemory.v1.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    @Transactional
    public UserRspDto signUp(UserReqDto reqDto) {
        checkNotNull(reqDto, "가입할 사용자 정보가 입력되지 않았습니다.");

        return insertUser(reqDto.toEntity())
                .map(user -> {
                    var privateRoomId = roomService.insertPrivateRoom(user.getId());
                    user.updatePrivateRoomId(privateRoomId);

                    return new UserRspDto(user);
                })
                .orElseThrow(UserInternalServerException::new);
    }

    @Transactional
    public UserRspDto signIn(int snsType, String snsId) {
        return findUser(snsType, snsId).map(UserRspDto::new)
                .orElseThrow(() -> new UserNotFoundException(
                                String.format("snsType: %d, snsId: %s", snsType, snsId)
                        )
                );
    }

    @Transactional
    public UserRspDto find(long userId) {
        return findUser(userId)
                .map(UserRspDto::new)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public UserRspDto patchToken(long userId, UserReqDto reqDto) {
        return findUser(userId).map(user ->
                user.changePushToken(reqDto.getPushToken())
                        .map(UserRspDto::new)
                        .orElseThrow(UserInternalServerException::new)
                )
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public UserRspDto update(long userId, UserReqDto reqDto) {
        return findUser(userId).map(user ->
                        user.updateUser(reqDto)
                                .map(UserRspDto::new)
                                .orElseThrow(UserInternalServerException::new)
                )
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public UserRspDto uploadProfileImage(long userId, UserReqDto reqDto) {
        // 1. Check image
        checkNotNull(reqDto.getProfileImage(), "업로드할 프로필이미지가 없습니다.");

        // 2. Get user
        var user = findUser(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 3. Check and delete exists image
        if (Objects.nonNull(user.getProfileImageUrl())) {
            s3Uploader.delete(user.getProfileImageUrl());
        }

        // 4. Upload image
        return Optional.ofNullable(s3Uploader.upload(reqDto.getProfileImage()))
                .map(url -> {
                    user.updateProfileImageUrl(url);
                    return new UserRspDto(user);
                })
                .orElseThrow(() -> new UserProfileImageUploadException("Failed upload image."));
    }

    @Transactional
    public UserRspDto deleteProfileImage(long userId) {
        return findUser(userId)
                .map(user ->
                    s3Uploader.delete(user.getProfileImageUrl())
                            .map(result -> user.updateProfileImageUrl(null))
                            .map(UserRspDto::new)
                            .orElseThrow(UserProfileImageUploadException::new)
                )
                .orElseThrow(() -> new UserNotFoundException(userId));
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
    public UserRspDto delete(long userId) {
        findUser(userId)
                // 1. Delete related friends
                .map(user -> {
                    findFriendsByUserOrFriendUser(user, user)
                            .ifPresent(friends -> friends.forEach(this::deleteFriend));

                    return user;
                })
                // 2. Delete related room
                .map(user -> {
                    user.getRooms().forEach(room -> {
                        var members = room.getUsers();

                        if (members.size() > 1) {
                            var owner = room.getOwner();
                            // Related rooms - 1) owner room
                            if (owner.equals(user)) {
                                transferOwner(room.getId(), owner.getId(), members);
                            }

                            // Related rooms - 1) owner room, 2) participant room
                            room.deleteUser(user);
                        }

                        // Related rooms - 3) private room
                        if (room.getId().longValue() == user.getPrivateRoomId()) {
                            roomService.delete(user.getPrivateRoomId(), user.getId());
                        }
                    });
                    user.deleteRooms(user.getRooms());

                    return user;
                })
                // Delete user after all job executed.
                .map(user -> {
                    user.deleteUser();
                    return true;
                })
                .orElseThrow(() -> new UserNotFoundException(userId));

        return null;
    }

    private void transferOwner(long roomId, long ownerId, List<User> users) {
        var transferIds = users.stream().map(User::getId).filter(id -> id != ownerId)
                .collect(Collectors.toList());
        Optional.of(transferIds)
                .filter(ids -> !ids.isEmpty())
                .map(ids -> ids.get(0))
                .ifPresent(id -> roomService.recommendOwner(roomId, id));
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

    /**
     * Friend Repository
     *
     * When working with a service code, the service code is connected to each other 
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<List<Friend>> findFriendsByUserOrFriendUser(User user, User friendUser) {
        return Optional.ofNullable(user)
                .flatMap(u -> friendRepository.findAllByUserOrFriendUser(user, friendUser));
    }

    private void deleteFriend(Friend friend) {
        friendRepository.delete(friend);
    }
}
