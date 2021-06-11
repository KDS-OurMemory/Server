package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.FriendInternalServerException;
import com.kds.ourmemory.advice.v1.friend.exception.FriendNotFoundFriendException;
import com.kds.ourmemory.advice.v1.friend.exception.FriendNotFoundUserException;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.friend.dto.DeleteFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.InsertFriendDto;
import com.kds.ourmemory.controller.v1.friend.dto.RequestFriendDto;
import com.kds.ourmemory.controller.v1.notice.dto.InsertNoticeDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.notice.NoticeType;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.friend.FriendRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FcmService;
import com.kds.ourmemory.service.v1.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@RequiredArgsConstructor
@Service
public class FriendService {
    private final FriendRepository friendRepo;

    // Add to work in rooms and user relationship tables
    private final UserRepository userRepo;

    // Add to FCM
    private final FcmService fcmService;

    // Add to Notice
    private final NoticeService noticeService;

    public RequestFriendDto.Response requestFriend(RequestFriendDto.Request request) {
        checkNotNull(request.getFriendId(), "친구 요청할 사용자가 없습니다. 사용자 번호 입력해주세요.");
        findFriend(request.getUserId(), request.getFriendId())
            .ifPresent(friend -> {
                checkArgument(!friend.getStatus().equals(FriendStatus.WAIT),
                        "이미 친구 요청한 사람입니다. 다른 사람의 회원 번호를 입력해주시기 바랍니다.");
                checkArgument(!friend.getStatus().equals(FriendStatus.REQUESTED_BY),
                        "이미 친구 요청을 받은 사람입니다. 친구 요청에 먼저 응답하시기 바랍니다.");
                checkArgument(!friend.getStatus().equals(FriendStatus.FRIEND),
                        "이미 친구 추가된 사람입니다. 다른 사람의 회원 번호를 입력해주시기 바랍니다.");
                checkArgument(!friend.getStatus().equals(FriendStatus.BLOCK),
                        "차단한 사람입니다.");
            });

        findFriend(request.getFriendId(), request.getUserId())
                .ifPresent(friend -> {
                    checkArgument(!friend.getStatus().equals(FriendStatus.FRIEND),
                            "이미 친구 요청을 수락한 사람입니다. 친구 추가를 진행해주시기 바랍니다.");
                    checkArgument(!friend.getStatus().equals(FriendStatus.BLOCK),
                            "상대방이 차단하여 친구 요청을 할 수 없습니다.");
                });

        return findUser(request.getUserId())
                .map(user -> {
                    User foundUser = findUser(request.getFriendId())
                            .map(friend -> {
                                // Insert to friend WAIT
                                insertFriend(new Friend(user, friend, FriendStatus.WAIT))
                                        .orElseThrow(() -> new FriendInternalServerException(String.format(
                                                "Insert Friend failed. [userId: %d, friendId: %d, status: %s]",
                                                request.getUserId(), request.getFriendId(), FriendStatus.WAIT)));

                                // Insert to friend REQUESTED_BY
                                insertFriend(new Friend(friend, user, FriendStatus.REQUESTED_BY))
                                        .orElseThrow(() -> new FriendInternalServerException(String.format(
                                                "Insert Friend failed. [userId: %d, friendId: %d, status: %s]",
                                                request.getUserId(), request.getFriendId(), FriendStatus.REQUESTED_BY)));

                                // Insert to Notices
                                String title = "OurMemory - 친구 요청";
                                String body = String.format("%s 이(가) 친구 요청하였습니다.", user.getName());
                                String friendToken = friend.getPushToken();

                                InsertNoticeDto.Request insertNoticeRequest = new InsertNoticeDto.Request(
                                        friend.getId(), NoticeType.FRIEND_REQUEST, Long.toString(request.getUserId()));
                                noticeService.insert(insertNoticeRequest);

                                // SendMessage to fcm
                                fcmService.sendMessageTo(
                                        new FcmDto.Request(friendToken, friend.getDeviceOs(), title, body,
                                                false, NoticeType.FRIEND_REQUEST.name(),
                                                Long.toString(request.getUserId())));

                                return friend;
                            })
                            .orElseThrow(() -> new FriendNotFoundFriendException(
                                    "Not found user matched friendId: " + request.getFriendId()));

                    return new RequestFriendDto.Response(foundUser.formatRegDate());
                })
                .orElseThrow(() ->
                        new FriendNotFoundUserException("Not found user matched userId:" + request.getUserId()));
    }

    public InsertFriendDto.Response addFriend(InsertFriendDto.Request request) {
        checkNotNull(request.getFriendId(), "추가할 친구가 없습니다. 친구 번호를 입력해주세요.");
        findFriend(request.getUserId(), request.getFriendId())
                .ifPresent(friend -> {
                    checkArgument(!friend.getStatus().equals(FriendStatus.FRIEND),
                            "이미 추가된 친구입니다. 다른 사람의 회원 번호를 입력해주시기 바랍니다.");
                    checkArgument(!friend.getStatus().equals(FriendStatus.BLOCK), "차단한 사람입니다.");
                });

        return findFriend(request.getUserId(), request.getFriendId())
                .map(fu -> {
                    Friend friend = findFriend(request.getFriendId(), request.getUserId())
                            .map(ff -> {
                                fu.changeStatus(FriendStatus.FRIEND)
                                        .map(this::updateFriend)
                                        .orElseThrow(() -> new FriendInternalServerException(String.format(
                                                "Update Friend failed to status set %s. [userId: %d, friendId: %d]",
                                                FriendStatus.FRIEND, request.getUserId(), request.getFriendId())));

                                return ff.changeStatus(FriendStatus.FRIEND)
                                        .map(f -> updateFriend(f).isPresent()? f: null)
                                        .orElseThrow(() -> new FriendInternalServerException(String.format(
                                                "Update Friend failed to status set %s. [userId: %d, friendId: %d]",
                                                FriendStatus.FRIEND, request.getFriendId(), request.getUserId())));
                            })
                            .orElseThrow(() -> new FriendNotFoundFriendException(
                                    String.format("Not found friend matched userId '%d' and friendId '%d'.",
                                            request.getFriendId(), request.getUserId())));

                    return new InsertFriendDto.Response(friend.formatRegDate());
                })
                .orElseThrow(() -> new FriendNotFoundFriendException(
                        String.format("Not found friend matched userId '%d' and friendId '%d'.",
                                request.getUserId(), request.getFriendId())));
    }

    // Not found friend -> None Error, just empty -> return emptyList
    public List<User> findFriends(long userId) {
        return findFriendsByUserId(userId)
                .map(friends -> friends.stream()
                        .filter(friend -> friend.getStatus().equals(FriendStatus.FRIEND)
                                || friend.getStatus().equals(FriendStatus.BLOCK))
                        .map(Friend::getFriend).collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    public DeleteFriendDto.Response delete(long userId, DeleteFriendDto.Request request) {
        checkNotNull(request.getFriendId(), "삭제할 친구가 없습니다. 친구 번호를 입력해주세요.");

        return findFriend(request.getFriendId(), userId)
                .map(friend -> {
                    // Delete friend only my side. The other side does not delete.
                    deleteFriend(friend);
                    return new DeleteFriendDto.Response(BaseTimeEntity.formatNow());
                })
                .orElseThrow(() -> new FriendNotFoundFriendException(String.format(
                                "Not found Friend matched friendId '%d', userId '%d'", request.getFriendId(), userId)));
    }

    /**
     * Friend Repository
     */
    private Optional<Friend> insertFriend(Friend friend) {
        return Optional.of(friendRepo.save(friend));
    }

    private Optional<Friend> findFriend(Long friendId, Long userId) {
        return Optional.ofNullable(friendId)
                .flatMap(f -> friendRepo.findByFriendIdAndUserId(friendId, userId));
    }

    private Optional<List<Friend>> findFriendsByUserId(Long userId) {
        return Optional.ofNullable(userId)
                .flatMap(friendRepo::findByUserId);
    }

    private Optional<Friend> updateFriend(Friend friend) {
        return Optional.of(friendRepo.save(friend));
    }

    private void deleteFriend(Friend friend) {
        friendRepo.delete(friend);
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
