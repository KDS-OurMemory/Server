package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.friend.dto.*;
import com.kds.ourmemory.controller.v1.notice.dto.InsertNoticeDto;
import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.friend.FriendStatus;
import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.entity.notice.NoticeType;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.friend.FriendRepository;
import com.kds.ourmemory.repository.notice.NoticeRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FcmService;
import com.kds.ourmemory.service.v1.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class FriendService {
    private final FriendRepository friendRepository;

    // Add to work in friend and user relationship tables
    private final UserRepository userRepo;

    // Add to work in friend and notice relationship tables
    private final NoticeRepository noticeRepo;

    // Add to FCM
    private final FcmService fcmService;

    // Add to Notice
    private final NoticeService noticeService;

    private static final String NOT_FOUND_MESSAGE = "Not found %s matched id: %d";

    private static final String NOT_FOUND_FRIEND_MESSAGE = "Not found Friend matched userId '%d', friendId '%d'";

    private static final String STATUS_ERROR_MESSAGE = "Friend status must be '%s'. friendStatus: %s";

    private static final String NOT_REQUESTED_ERROR = "Addition cannot be proceeded without a friend request.";

    private static final String USER_ALREADY_FRIEND = "User '%d' already friend. delete request plz.";

    private static final String USER = "user";

    public FriendDto requestFriend(RequestFriendDto.Request request) {
        findFriend(request.getUserId(), request.getFriendUserId())
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
                () -> new FriendNotFoundUserException(
                        String.format(NOT_FOUND_MESSAGE, USER, request.getUserId())
                )
        );
        var friend = findUser(request.getFriendUserId()).orElseThrow(
                () -> new FriendNotFoundFriendException(
                        String.format(NOT_FOUND_MESSAGE, USER, request.getFriendUserId())
                )
        );

        // Add my side WAIT status
        var insertFriendMySideRsp = insertFriend(new Friend(user, friend, FriendStatus.WAIT));

        // Check Already friend
        return findFriend(request.getFriendUserId(), request.getUserId())
                .map(friendSideFriend ->
                        Optional.of(friendSideFriend)
                                .filter(f -> f.getStatus().equals(FriendStatus.BLOCK))
                                .map(f -> new FriendDto(insertFriendMySideRsp))  // response by request result
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

                    return new FriendDto(insertFriendMySideRsp); // response by request result
                });
    }

    @Transactional
    public FriendDto cancelFriend(CancelFriendDto.Request request) {
        // Check my side
        findFriend(request.getUserId(), request.getFriendUserId())
                .map(friend -> {
                    FriendStatus status = friend.getStatus();
                    if (status.equals(FriendStatus.FRIEND) || status.equals(FriendStatus.BLOCK)) {
                        throw new FriendInternalServerException(
                                String.format(USER_ALREADY_FRIEND, request.getFriendUserId())
                        );
                    } else if (!FriendStatus.WAIT.equals(status)) {
                        throw new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.WAIT, status)
                        );
                    }

                    deleteFriend(friend);

                    return friend;
                })
                .orElseThrow(
                        () -> new FriendNotFoundException(
                                String.format(NOT_FOUND_FRIEND_MESSAGE, request.getUserId(), request.getFriendUserId())
                        )
                );

        // Check friend side And delete
        return findFriend(request.getFriendUserId(), request.getUserId())
                .map(friend -> {
                    FriendStatus status = friend.getStatus();
                    switch (status) {
                        case REQUESTED_BY -> {
                            deleteFriend(friend);

                            // Delete friend request notice from accepted user
                            noticeService.findNotices(friend.getUser().getId(), false)
                                    .forEach(findNoticesRsp -> {
                                        if (NoticeType.FRIEND_REQUEST.equals(findNoticesRsp.getType())
                                                && findNoticesRsp.getValue().equals(Long.toString(friend.getFriendUser().getId()))
                                        ) {
                                            noticeService.deleteNotice(findNoticesRsp.getNoticeId());
                                        }
                                    });
                        }
                        case BLOCK -> {
                        }
                        case FRIEND -> throw new FriendInternalServerException(
                                String.format(USER_ALREADY_FRIEND, request.getUserId())
                        );
                        default -> throw new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.REQUESTED_BY, status)
                        );
                    }

                    return new FriendDto(); // TODO: 삭제 로직을 삭제 플래그로 수정 후 데이터 수정 예정
                })
                .orElseThrow(
                        () -> new FriendNotFoundException(
                                String.format(NOT_FOUND_FRIEND_MESSAGE, request.getFriendUserId(), request.getUserId())
                        )
                );
    }

    @Transactional
    public FriendDto acceptFriend(AcceptFriendDto.Request request) {
        // Check friend status on accept side
        var acceptFriend = findFriend(request.getUserId(), request.getFriendUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.REQUESTED_BY))
                        .orElseThrow(() -> new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.REQUESTED_BY.name(), fa.getStatus().name()))
                        )
                )
                .orElseThrow(() -> new FriendNotRequestedException(NOT_REQUESTED_ERROR));

        // Check friend status on request side
        var requestFriend = findFriend(request.getFriendUserId(), request.getUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.WAIT))
                        .orElseThrow(() -> new FriendStatusException(
                                        String.format(STATUS_ERROR_MESSAGE, FriendStatus.WAIT.name(), fa.getStatus().name())
                                )
                        )
                )
                .orElseThrow(() -> new FriendNotRequestedException(NOT_REQUESTED_ERROR));

        // Accept request
        acceptFriend.changeStatus(FriendStatus.FRIEND)
                .orElseThrow(() -> new FriendInternalServerException("Failed to update for friend status data."));
        requestFriend.changeStatus(FriendStatus.FRIEND)
                .orElseThrow(() -> new FriendInternalServerException("Failed to update for friend status data."));

        // Read related notice from accept user
        findNoticesByUserId(request.getUserId())
                .ifPresent(notices -> notices.forEach(notice -> {
                            if (NoticeType.FRIEND_REQUEST.equals(notice.getType())
                                    && notice.getValue().equals(Long.toString(request.getFriendUserId()))
                            ) {
                                notice.readNotice();
                            }
                        })
                );

        return new FriendDto(acceptFriend);
    }

    public FriendDto reAddFriend(ReAddFriendDto.Request request) {
        // Check friend status on friend side
        findFriend(request.getFriendUserId(), request.getUserId())
                .map(ff -> {
                    if (ff.getStatus().equals(FriendStatus.BLOCK))
                        throw new FriendBlockedException("Blocked by friend.");

                    if (!ff.getStatus().equals(FriendStatus.FRIEND))
                        throw new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.FRIEND.name(), ff.getStatus().name())
                        );

                    return ff;
                })
                .orElseThrow(() -> new FriendInternalServerException(
                        String.format("Cannot add friend '%d' because friend side is not a friend to user '%d'",
                                request.getFriendUserId(), request.getUserId())
                ));

        // Check friend status on request side, And add friend
        return findFriend(request.getUserId(), request.getFriendUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.WAIT))
                        .map(f -> {
                            f.changeStatus(FriendStatus.FRIEND).ifPresent(this::updateFriend);
                            return new FriendDto(f);
                        })
                        .orElseThrow(() -> new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.WAIT.name(), fa.getStatus().name()))
                        )
                )
                .orElseThrow(() -> new FriendNotRequestedException(NOT_REQUESTED_ERROR));
    }

    // Not found friend -> None Error, just empty -> return emptyList
    public List<FriendDto> findFriends(long userId) {
        return findFriendsByUserId(userId)
                .map(friends -> friends.stream()
                        .map(FriendDto::new)
                        .collect(Collectors.toList())
                )
                .orElseGet(ArrayList::new);
    }

    public FriendDto patchFriendStatus(PatchFriendStatusDto.Request request) {
        return findFriend(request.getUserId(), request.getFriendUserId())
                .map(friend ->
                        Optional.ofNullable(request.getStatus())
                                .filter(status -> !(status.equals(FriendStatus.WAIT) || status.equals(FriendStatus.REQUESTED_BY)))
                                .map(status -> {
                                    friend.changeStatus(status);
                                    updateFriend(friend);
                                    return new FriendDto(friend);
                                })
                                .orElseThrow(() -> new FriendStatusException(
                                        String.format("Friend status cannot be '%s' and '%s'", FriendStatus.WAIT, FriendStatus.REQUESTED_BY))
                                )
                )
                .orElseThrow(() -> new FriendNotFoundFriendException(
                                String.format(NOT_FOUND_FRIEND_MESSAGE, request.getUserId(), request.getFriendUserId())
                        )
                );
    }

    public FriendDto deleteFriend(long userId, long friendUserId) {
        return findFriend(userId, friendUserId)
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.WAIT) || friend.getStatus().equals(FriendStatus.REQUESTED_BY))
                        throw new FriendInternalServerException(
                                String.format("User '%d' is not friend. cancel request plz.", friendUserId)
                        );

                    // Delete friend only my side. The other side does not delete.
                    deleteFriend(friend);
                    return new FriendDto(); // TODO: 삭제 로직을 삭제 플래그로 수정 후 데이터 수정 예정
                })
                .orElseThrow(() -> new FriendNotFoundFriendException(
                                String.format(NOT_FOUND_FRIEND_MESSAGE, userId, friendUserId)
                        )
                );
    }

    /**
     * Friend Repository
     */
    private Friend insertFriend(Friend friend) {
        return friendRepository.save(friend);
    }

    private Optional<Friend> findFriend(Long userId, Long friendId) {
        return Optional.ofNullable(userId)
                .flatMap(f -> friendRepository.findByUserIdAndFriendUserId(userId, friendId));
    }

    private Optional<List<Friend>> findFriendsByUserId(Long userId) {
        return Optional.ofNullable(userId)
                .flatMap(friendRepository::findAllByUserId);
    }

    private void updateFriend(Friend friend) {
        friendRepository.save(friend);
    }

    private void deleteFriend(Friend friend) {
        friendRepository.delete(friend);
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

    /**
     * Notice Repository
     * <p>
     * When working with a service code, the service code is connected to each other
     * and is caught in an infinite loop in the injection of dependencies.
     */
    private Optional<List<Notice>> findNoticesByUserId(Long userId) {
        List<Notice> notices = new ArrayList<>();

        noticeRepo.findAllByUserId(userId).ifPresent(noticeList -> notices.addAll(
                noticeList.stream().filter(Notice::isUsed).collect(toList()))
        );

        return Optional.of(notices)
                .filter(noticeList -> !noticeList.isEmpty());
    }
}
