package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.FriendInternalServerException;
import com.kds.ourmemory.advice.v1.friend.exception.FriendNotFoundFriendException;
import com.kds.ourmemory.advice.v1.friend.exception.FriendNotFoundUserException;
import com.kds.ourmemory.controller.v1.friend.dto.InsertFriendDto;
import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.friend.FriendRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@RequiredArgsConstructor
@Service
public class FriendService {
    private final FriendRepository friendRepo;

    // Add to work in rooms and user relationship tables
    private final UserRepository userRepo;

    public InsertFriendDto.Response addFriend(long userId, InsertFriendDto.Request request) {
        checkNotNull(request.getFriendId(), "추가할 친구가 없습니다. 친구 번호를 입력해주세요.");

        return findUser(userId)
                .map(user -> {
                    Friend insertedFriend = findUser(request.getFriendId())
                            .map(friend -> {
                                insertFriend(new Friend(user, friend))
                                        .orElseThrow(() -> new FriendInternalServerException(String.format(
                                                "Insert Friend failed. [userId: %d, friendId: %d]", userId, request.getFriendId())));

                                return insertFriend(new Friend(friend, user))
                                        .orElseThrow(() -> new FriendInternalServerException(String.format(
                                                "Insert Friend failed. [userId: %d, friendId: %d]", request.getFriendId(), userId)));
                            })
                            .orElseThrow(() -> new FriendNotFoundFriendException("Not found user matched friendId: " + request.getFriendId()));

                    return new InsertFriendDto.Response(insertedFriend.formatRegDate());
                })
                .orElseThrow(() -> new FriendNotFoundUserException("Not found user matched userId:" + userId));
    }

    // Not found friend -> None Error, just empty -> return emptyList
    public List<User> findFriends(long userId) {
        return findFriendsByUserId(userId)
                .map(friends -> friends.stream().map(Friend::getFriend).collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    /**
     * Friend Repository
     */
    private Optional<Friend> insertFriend(Friend friend) {
        return Optional.of(friendRepo.save(friend));
    }

    private Optional<List<Friend>> findFriendsByUserId(Long userId) {
        return Optional.ofNullable(userId).flatMap(friendRepo::findByUserId);
    }

    /**
     * User Repository
     * <p>
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepo::findById);
    }
}
