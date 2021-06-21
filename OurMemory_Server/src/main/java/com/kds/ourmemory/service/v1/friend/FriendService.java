package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.friend.dto.*;
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
        findFriend(request.getUserId(), request.getFriendId())
                .ifPresent(friend -> {
                    checkArgument(!friend.getStatus().equals(FriendStatus.WAIT),
                            "이미 친구 요청한 사람입니다. 다른 사람의 회원 번호를 입력해주시기 바랍니다.");
                    checkArgument(!friend.getStatus().equals(FriendStatus.REQUESTED_BY),
                            "이미 친구 요청을 받은 사람입니다. 친구 요청에 먼저 응답하시기 바랍니다.");
                    checkArgument(!friend.getStatus().equals(FriendStatus.FRIEND),
                            "이미 친구 추가된 사람입니다. 다른 사람의 회원 번호를 입력해주시기 바랍니다.");
                    checkArgument(!friend.getStatus().equals(FriendStatus.BLOCK),
                            "차단한 사람입니다. 다른 사람의 회원 번호를 입력해주시기 바랍니다.");
                });

        var user = findUser(request.getUserId()).orElseThrow(
                () -> new FriendNotFoundUserException("Not found user matched userId: " + request.getUserId()));
        var friend = findUser(request.getFriendId()).orElseThrow(
                () -> new FriendNotFoundFriendException("Not found user matched friendId: " + request.getFriendId()));

        // Add my side WAIT status
        insertFriend(new Friend(user, friend, FriendStatus.WAIT));

        // Check Already friend
        return findFriend(request.getFriendId(), request.getUserId())
                .map(friendSideFriend ->
                        Optional.of(friendSideFriend)
                                .filter(f -> f.getStatus().equals(FriendStatus.BLOCK))
                                .map(f -> new RequestFriendDto.Response(BaseTimeEntity.formatNow()))
                                .orElseThrow(() -> new FriendAlreadyAcceptException("Already accepted a friend request.")))
                // Add friend side REQUESTED_BY status
                .orElseGet(() -> {
                    insertFriend(new Friend(friend, user, FriendStatus.REQUESTED_BY));

                    // Insert to Notices
                    var title = "OurMemory - 친구 요청";
                    var body = String.format("%s 이(가) 친구 요청하였습니다.", user.getName());
                    var friendToken = friend.getPushToken();

                    var insertNoticeRequest = new InsertNoticeDto.Request(
                            friend.getId(), NoticeType.FRIEND_REQUEST, Long.toString(request.getUserId()));
                    noticeService.insert(insertNoticeRequest);

                    // SendMessage to fcm
                    fcmService.sendMessageTo(
                            new FcmDto.Request(friendToken, friend.getDeviceOs(), title, body,
                                    false, NoticeType.FRIEND_REQUEST.name(),
                                    Long.toString(request.getUserId())));

                    return new RequestFriendDto.Response(BaseTimeEntity.formatNow());
                });
    }

    public CancelFriendDto.Response cancelFriend(CancelFriendDto.Request request) {
        // Check my side
        var mySideFriend = findFriend(request.getUserId(), request.getFriendId())
                .map(friend -> {
                    FriendStatus status = friend.getStatus();
                    if (status.equals(FriendStatus.FRIEND) || status.equals(FriendStatus.BLOCK))
                        throw new FriendInternalServerException(
                                String.format("User '%d' already friend. delete request plz.", request.getFriendId())
                        );

                    return friend;
                })
                .orElseThrow(
                        () -> new FriendNotFoundException(String.format("Not found Friend matched userId '%d', friendId '%d'",
                                request.getUserId(), request.getFriendId()))
                );

        // Check friend side And delete
        return findFriend(request.getFriendId(), request.getUserId())
                .map(friend -> {
                    FriendStatus status = friend.getStatus();
                    if (status.equals(FriendStatus.FRIEND)) {
                        throw new FriendInternalServerException(
                                String.format("User '%d' already friend. delete request plz.", request.getUserId())
                        );
                    } else if (status.equals(FriendStatus.WAIT) || status.equals(FriendStatus.REQUESTED_BY)) {
                        deleteFriend(friend);
                    }

                    deleteFriend(mySideFriend);

                    return new CancelFriendDto.Response(BaseTimeEntity.formatNow());
                })
                .orElseThrow(
                        () -> new FriendNotFoundException(String.format("Not found Friend matched userId '%d', friendId '%d'",
                                request.getFriendId(), request.getUserId()))
                );
    }

    public AcceptFriendDto.Response acceptFriend(AcceptFriendDto.Request request) {
        // Check friend status on accept side
        var acceptFriend = findFriend(request.getAcceptUserId(), request.getRequestUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.REQUESTED_BY))
                        .orElseThrow(() -> new FriendStatusException(String.format("Friend status must be '%s'. friendStatus: %s",
                                FriendStatus.REQUESTED_BY.name(), fa.getStatus().name()))))
                .orElseThrow(() -> new FriendNotRequestedException("Addition cannot be proceeded without a friend request."));

        // Check friend status on request side
        var requestFriend = findFriend(request.getRequestUserId(), request.getAcceptUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.WAIT))
                        .orElseThrow(() -> new FriendStatusException(String.format("Friend status must be '%s'. friendStatus: %s",
                                FriendStatus.WAIT.name(), fa.getStatus().name()))))
                .orElseThrow(() -> new FriendNotRequestedException("Addition cannot be proceeded without a friend request."));

        // Accept request
        acceptFriend.changeStatus(FriendStatus.FRIEND).ifPresent(this::updateFriend);
        requestFriend.changeStatus(FriendStatus.FRIEND).ifPresent(this::updateFriend);

        return new AcceptFriendDto.Response(BaseTimeEntity.formatNow());
    }

    public ReAddFriendDto.Response reAddFriend(ReAddFriendDto.Request request) {
        // Check friend status on friend side
        findFriend(request.getFriendId(), request.getUserId())
                .map(ff -> {
                    if (ff.getStatus().equals(FriendStatus.BLOCK))
                        throw new FriendBlockedException("Blocked by friend.");

                    if (!ff.getStatus().equals(FriendStatus.FRIEND))
                        throw new FriendStatusException(String.format("Friend status must be '%s'. friendStatus: %s",
                                FriendStatus.FRIEND.name(), ff.getStatus().name()));

                    return ff;
                })
                .orElseThrow(() -> new FriendInternalServerException(
                        String.format("Cannot add friend '%d' because friend side is not a friend to user '%d'",
                                request.getFriendId(), request.getUserId())
                ));

        // Check friend status on request side, And add friend
        return findFriend(request.getUserId(), request.getFriendId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.WAIT))
                        .map(f -> {
                            f.changeStatus(FriendStatus.FRIEND).ifPresent(this::updateFriend);
                            return new ReAddFriendDto.Response(BaseTimeEntity.formatNow());
                        })
                        .orElseThrow(() -> new FriendStatusException(String.format("Friend status must be '%s'. friendStatus: %s",
                                FriendStatus.WAIT.name(), fa.getStatus().name()))))
                .orElseThrow(() -> new FriendNotRequestedException("Addition cannot be proceeded without a friend request."));
    }

    // Not found friend -> None Error, just empty -> return emptyList
    public List<Friend> findFriends(long userId) {
        return findFriendsByUserId(userId)
                .map(friends -> friends.stream()
                        .filter(friend -> friend.getStatus().equals(FriendStatus.FRIEND)
                                || friend.getStatus().equals(FriendStatus.BLOCK))
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    public PatchFriendStatusDto.Response patchFriendStatus(PatchFriendStatusDto.Request request) {
        return findFriend(request.getUserId(), request.getFriendId())
                .map(friend ->
                    Optional.ofNullable(request.getStatus())
                            .filter(status -> !(status.equals(FriendStatus.WAIT)|| status.equals(FriendStatus.REQUESTED_BY)))
                            .map(status -> {
                                friend.changeStatus(status);
                                updateFriend(friend);
                                return new PatchFriendStatusDto.Response(BaseTimeEntity.formatNow());
                            })
                            .orElseThrow(() -> new FriendStatusException(
                                    String.format("Friend status cannot be '%s' and '%s'", FriendStatus.WAIT, FriendStatus.REQUESTED_BY))
                            )
                )
                .orElseThrow(() -> new FriendNotFoundFriendException(
                        String.format("Not found Friend matched userId '%d', friendId '%d'", request.getUserId(), request.getFriendId()))
                );
    }

    public DeleteFriendDto.Response deleteFriend(DeleteFriendDto.Request request) {
        return findFriend(request.getUserId(), request.getFriendId())
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.WAIT) || friend.getStatus().equals(FriendStatus.REQUESTED_BY))
                        throw new FriendInternalServerException(
                                String.format("User '%d' is not friend. cancel request plz.", request.getFriendId())
                        );

                    // Delete friend only my side. The other side does not delete.
                    deleteFriend(friend);
                    return new DeleteFriendDto.Response(BaseTimeEntity.formatNow());
                })
                .orElseThrow(() -> new FriendNotFoundFriendException(
                        String.format("Not found Friend matched userId '%d', friendId '%d'", request.getUserId(), request.getFriendId()))
                );
    }

    /**
     * Friend Repository
     */
    private void insertFriend(Friend friend) {
        friendRepo.save(friend);
    }

    private Optional<Friend> findFriend(Long userId, Long friendId) {
        return Optional.ofNullable(userId)
                .flatMap(f -> friendRepo.findByUserIdAndFriendId(userId, friendId));
    }

    private Optional<List<Friend>> findFriendsByUserId(Long userId) {
        return Optional.ofNullable(userId)
                .flatMap(friendRepo::findByUserId);
    }

    private void updateFriend(Friend friend) {
        friendRepo.save(friend);
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
