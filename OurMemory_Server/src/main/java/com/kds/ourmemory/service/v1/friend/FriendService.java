package com.kds.ourmemory.service.v1.friend;

import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import com.kds.ourmemory.controller.v1.friend.dto.*;
import com.kds.ourmemory.controller.v1.notice.dto.NoticeReqDto;
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

    private static final String NOT_REQUESTED_ERROR = "Addition cannot be proceeded without a friend reqDto.";

    private static final String USER_ALREADY_FRIEND = "User '%d' already friend. delete reqDto plz.";

    private static final String USER = "user";

    public List<FriendRspDto> findUsers(long userId, Long targetId, String name, FriendStatus friendStatus) {
        // Find by friendStatus
        var responseList = findFriendsByUserId(userId)
                .map(list -> list.stream().filter(friend -> friend.getStatus().equals(friendStatus))
                        .map(friend -> new FriendRspDto(friend.getFriendUser(), friend))
                        .collect(Collectors.toList())
                )
                .orElseGet(ArrayList::new);

        // Find by friendId or name
        responseList.addAll(
                findUsersByIdOrName(targetId, name)
                        .map(users -> users.stream().map(user -> {
                                            Friend friend = findFriend(userId, targetId)
                                                    .orElse(null);
                                            return new FriendRspDto(user, friend);
                                        })
                                        .collect(Collectors.toList())
                        )
                        .orElseGet(ArrayList::new)
        );

        return responseList.stream().distinct().collect(Collectors.toList());
    }

    public FriendRspDto requestFriend(FriendReqDto reqDto) {
        findFriend(reqDto.getUserId(), reqDto.getFriendUserId())
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

        var user = findUser(reqDto.getUserId()).orElseThrow(
                () -> new FriendNotFoundUserException(
                        String.format(NOT_FOUND_MESSAGE, USER, reqDto.getUserId())
                )
        );
        var friend = findUser(reqDto.getFriendUserId()).orElseThrow(
                () -> new FriendNotFoundFriendException(
                        String.format(NOT_FOUND_MESSAGE, USER, reqDto.getFriendUserId())
                )
        );

        // Add my side WAIT status
        var insertFriendMySideRsp = insertFriend(new Friend(user, friend, FriendStatus.WAIT));

        // Check Already friend
        return findFriend(reqDto.getFriendUserId(), reqDto.getUserId())
                .map(friendSideFriend ->
                        Optional.of(friendSideFriend)
                                .filter(f -> f.getStatus().equals(FriendStatus.BLOCK))
                                .map(f -> new FriendRspDto(insertFriendMySideRsp))  // response by reqDto result
                                .orElseThrow(() -> new FriendAlreadyAcceptException("Already accepted a friend reqDto.")))
                // Add friend side REQUESTED_BY status
                .orElseGet(() -> {
                    insertFriend(new Friend(friend, user, FriendStatus.REQUESTED_BY));

                    // Insert to Notices
                    var title = "OurMemory - 친구 요청";
                    var body = String.format("%s 이(가) 친구 요청하였습니다.", user.getName());
                    var friendToken = friend.getPushToken();

                    var insertNoticeRequest = new NoticeReqDto(
                            friend.getId(), NoticeType.FRIEND_REQUEST, Long.toString(reqDto.getUserId()));
                    noticeService.insert(insertNoticeRequest);

                    // SendMessage to fcm
                    fcmService.sendMessageTo(
                            new FcmDto.Request(friendToken, friend.getDeviceOs(), title, body,
                                    false, NoticeType.FRIEND_REQUEST.name(),
                                    Long.toString(reqDto.getUserId())));

                    return new FriendRspDto(insertFriendMySideRsp); // response by reqDto result
                });
    }

    @Transactional
    public FriendRspDto cancelFriend(long userId, long friendUserId) {
        // Check my side
        findFriend(userId, friendUserId)
                .map(friend -> {
                    FriendStatus status = friend.getStatus();
                    if (status.equals(FriendStatus.FRIEND) || status.equals(FriendStatus.BLOCK)) {
                        throw new FriendInternalServerException(
                                String.format(USER_ALREADY_FRIEND, friendUserId)
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
                                String.format(NOT_FOUND_FRIEND_MESSAGE, userId, friendUserId)
                        )
                );

        // Check friend side And delete
        findFriend(friendUserId, userId)
                .map(friend -> {
                    FriendStatus status = friend.getStatus();
                    switch (status) {
                        case REQUESTED_BY -> {
                            deleteFriend(friend);

                            // Delete friend reqDto notice from accepted user
                            noticeService.findNotices(friend.getUser().getId(), false)
                                    .forEach(findNoticesRsp -> {
                                        if (NoticeType.FRIEND_REQUEST.equals(findNoticesRsp.getType())
                                                && findNoticesRsp.getValue().equals(Long.toString(friend.getFriendUser().getId()))
                                        ) {
                                            noticeService.delete(findNoticesRsp.getNoticeId());
                                        }
                                    });
                        }
                        case BLOCK -> {
                        }
                        case FRIEND -> throw new FriendInternalServerException(
                                String.format(USER_ALREADY_FRIEND, userId)
                        );
                        default -> throw new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.REQUESTED_BY, status)
                        );
                    }

                    return true;
                })
                .orElseThrow(
                        () -> new FriendNotFoundException(
                                String.format(NOT_FOUND_FRIEND_MESSAGE, friendUserId, userId)
                        )
                );

        // not exists friend data(cancel -> delete friend data), so null response.
        return null;
    }

    @Transactional
    public FriendRspDto acceptFriend(FriendReqDto reqDto) {
        // Check friend status on accept side
        var acceptFriend = findFriend(reqDto.getUserId(), reqDto.getFriendUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.REQUESTED_BY))
                        .orElseThrow(() -> new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.REQUESTED_BY.name(), fa.getStatus().name()))
                        )
                )
                .orElseThrow(() -> new FriendNotRequestedException(NOT_REQUESTED_ERROR));

        // Check friend status on reqDto side
        var requestFriend = findFriend(reqDto.getFriendUserId(), reqDto.getUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.WAIT))
                        .orElseThrow(() -> new FriendStatusException(
                                        String.format(STATUS_ERROR_MESSAGE, FriendStatus.WAIT.name(), fa.getStatus().name())
                                )
                        )
                )
                .orElseThrow(() -> new FriendNotRequestedException(NOT_REQUESTED_ERROR));

        // Accept reqDto
        acceptFriend.changeStatus(FriendStatus.FRIEND)
                .orElseThrow(() -> new FriendInternalServerException("Failed to update for friend status data."));
        requestFriend.changeStatus(FriendStatus.FRIEND)
                .orElseThrow(() -> new FriendInternalServerException("Failed to update for friend status data."));

        // Read related notice from accept user
        findNoticesByUserId(reqDto.getUserId())
                .ifPresent(notices -> notices.forEach(notice -> {
                            if (NoticeType.FRIEND_REQUEST.equals(notice.getType())
                                    && notice.getValue().equals(Long.toString(reqDto.getFriendUserId()))
                            ) {
                                notice.readNotice();
                            }
                        })
                );

        return new FriendRspDto(acceptFriend);
    }

    public FriendRspDto reAddFriend(FriendReqDto reqDto) {
        // Check friend status on friend side
        findFriend(reqDto.getFriendUserId(), reqDto.getUserId())
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
                                reqDto.getFriendUserId(), reqDto.getUserId())
                ));

        // Check friend status on reqDto side, And add friend
        return findFriend(reqDto.getUserId(), reqDto.getFriendUserId())
                .map(fa -> Optional.of(fa).filter(f -> f.getStatus().equals(FriendStatus.WAIT))
                        .map(f -> {
                            f.changeStatus(FriendStatus.FRIEND).ifPresent(this::updateFriend);
                            return new FriendRspDto(f);
                        })
                        .orElseThrow(() -> new FriendStatusException(
                                String.format(STATUS_ERROR_MESSAGE, FriendStatus.WAIT.name(), fa.getStatus().name()))
                        )
                )
                .orElseThrow(() -> new FriendNotRequestedException(NOT_REQUESTED_ERROR));
    }

    // Not found friend -> None Error, just empty -> return emptyList
    public List<FriendRspDto> findFriends(long userId) {
        return findFriendsByUserId(userId)
                .map(friends -> friends.stream()
                        .map(FriendRspDto::new)
                        .collect(Collectors.toList())
                )
                .orElseGet(ArrayList::new);
    }

    public FriendRspDto patchFriendStatus(FriendReqDto reqDto) {
        return findFriend(reqDto.getUserId(), reqDto.getFriendUserId())
                .map(friend ->
                        Optional.ofNullable(reqDto.getFriendStatus())
                                .filter(status -> !(status.equals(FriendStatus.WAIT) || status.equals(FriendStatus.REQUESTED_BY)))
                                .map(status -> {
                                    friend.changeStatus(status);
                                    updateFriend(friend);
                                    return new FriendRspDto(friend);
                                })
                                .orElseThrow(() -> new FriendStatusException(
                                        String.format("Friend status cannot be '%s' and '%s'", FriendStatus.WAIT, FriendStatus.REQUESTED_BY))
                                )
                )
                .orElseThrow(() -> new FriendNotFoundFriendException(
                                String.format(NOT_FOUND_FRIEND_MESSAGE, reqDto.getUserId(), reqDto.getFriendUserId())
                        )
                );
    }

    public FriendRspDto deleteFriend(long userId, long friendUserId) {
        findFriend(userId, friendUserId)
                .map(friend -> {
                    if (friend.getStatus().equals(FriendStatus.WAIT) || friend.getStatus().equals(FriendStatus.REQUESTED_BY))
                        throw new FriendInternalServerException(
                                String.format("User '%d' is not friend. cancel reqDto plz.", friendUserId)
                        );

                    // Delete friend only my side. The other side does not delete.
                    deleteFriend(friend);
                    return true;
                })
                .orElseThrow(() -> new FriendNotFoundFriendException(
                                String.format(NOT_FOUND_FRIEND_MESSAGE, userId, friendUserId)
                        )
                );

        // delete response is null -> client already have data, so don't need response data.
        return null;
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

    private Optional<List<User>> findUsersByIdOrName(Long userId, String name) {
        return Optional.ofNullable(userRepo.findAllByUsedAndIdOrName(true, userId, name))
                .filter(users -> users.isPresent() && !users.get().isEmpty())
                .orElseGet(Optional::empty);
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
