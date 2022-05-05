package com.kds.ourmemory.notice.v1.service;


import com.kds.ourmemory.notice.v1.advice.exception.NoticeInternalServerException;
import com.kds.ourmemory.notice.v1.advice.exception.NoticeNotFoundException;
import com.kds.ourmemory.notice.v1.advice.exception.NoticeNotFoundUserException;
import com.kds.ourmemory.notice.v1.controller.dto.NoticeReqDto;
import com.kds.ourmemory.notice.v1.controller.dto.NoticeRspDto;
import com.kds.ourmemory.notice.v1.entity.Notice;
import com.kds.ourmemory.user.v1.entity.User;
import com.kds.ourmemory.notice.v1.repository.NoticeRepository;
import com.kds.ourmemory.user.v1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class NoticeService {
    private final NoticeRepository noticeRepo;

    // Add to work in memory and user relationship tables
    private final UserRepository userRepo;

    @Transactional
    public NoticeRspDto insert(NoticeReqDto reqDto) {
        return findUser(reqDto.getUserId())
                .map(user -> insertNotice(reqDto.toEntity(user))
                        .map(NoticeRspDto::new)
                        .orElseThrow(() -> new NoticeInternalServerException(
                                String.format("Notice [type: %s, value: %s] insert failed.",
                                        reqDto.getNoticeType(), reqDto.getNoticeValue())
                        ))
                )
                .orElseThrow(() -> new NoticeNotFoundUserException(reqDto.getUserId()));
    }

    @Transactional
    public List<NoticeRspDto> findNotices(long userId, boolean isReadProcessing) {
        return findNoticesByUserId(userId)
                .map(notices -> notices.stream().map(notice -> {
                    // new response before read processing
                    var response = new NoticeRspDto(notice);

                    if (isReadProcessing) {
                        notice.readNotice();
                    }
                    return response;
                }).collect(toList()))
                .orElseGet(ArrayList::new);
    }

    @Transactional
    public NoticeRspDto delete(long id) {
        findNotice(id)
                .map(notice -> {
                    notice.deleteNotice();
                    return updateNotice(notice).map(n -> true)
                            .orElseThrow(() ->
                                    new NoticeInternalServerException("Failed to update for notice used set false."));
                })
                .orElseThrow(() -> new NoticeNotFoundException(id));

        // delete response is null -> client already have data, so don't need response data.
        return null;
    }

    /**
     * Notice Repository
     */
    private Optional<Notice> insertNotice(Notice notice) {
        return Optional.of(noticeRepo.save(notice));
    }

    private Optional<Notice> findNotice(Long id) {
        return Optional.ofNullable(id).flatMap(noticeRepo::findById);
    }

    private Optional<List<Notice>> findNoticesByUserId(Long userId) {
        List<Notice> notices = new ArrayList<>();

        noticeRepo.findAllByUserId(userId).ifPresent(noticeList -> notices.addAll(
                        noticeList.stream().filter(Notice::isUsed).collect(toList()))
                );

        return Optional.of(notices)
                .filter(noticeList -> !noticeList.isEmpty());
    }

    private Optional<Notice> updateNotice(Notice notice) {
        return Optional.of(noticeRepo.save(notice));
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
