package com.kds.ourmemory.service.v1.notice;

import com.kds.ourmemory.advice.v1.notice.exception.NoticeInternalServerException;
import com.kds.ourmemory.advice.v1.notice.exception.NoticeNotFoundUserException;
import com.kds.ourmemory.controller.v1.notice.dto.InsertNoticeDto;
import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.notice.NoticeRepository;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@RequiredArgsConstructor
@Service
public class NoticeService {
    private final NoticeRepository noticeRepo;

    // Add to work in memory and user relationship tables
    private final UserRepository userRepo;

    @Transactional
    public InsertNoticeDto.Response insert(InsertNoticeDto.Request request) {
        checkNotNull(request.getUserId(), "알림 사용자 번호가 입력되지 않았습니다. 알림 대상 사용자의 번호를 입력해주세요.");
        checkNotNull(request.getType(), "알림 종류가 입력되지 않았습니다. 알림 종류를 입력해주세요.");
        checkNotNull(request.getValue(), "알림 문자열 값이 입력되지 않았습니다. 알림 문자열 값을 입력해주세요.");

        return findUser(request.getUserId())
                .map(user -> {
                    Notice notice = Notice.builder()
                            .user(user)
                            .type(request.getType())
                            .value(request.getValue())
                            .build();

                    Notice insertedNotice = insertNotice(notice)
                            .orElseThrow(() -> new NoticeInternalServerException(
                                    String.format("Notice [type: %s, value: %s] insert failed.",
                                            request.getType(), request.getValue())
                            ));
                    return new InsertNoticeDto.Response(insertedNotice.formatRegDate());
                })
                .orElseThrow(() -> new NoticeNotFoundUserException(
                        "Not found user matched to userId: " + request.getUserId()));
    }

    public List<Notice> findNotices(long userId) {
        return findNoticesByUserId(userId)
                .orElseGet(ArrayList::new);
    }

    /**
     * Notice Repository
     */
    private Optional<Notice> insertNotice(Notice notice) {
        return Optional.of(noticeRepo.save(notice));
    }

    private Optional<List<Notice>> findNoticesByUserId(Long userId) {
        return Optional.ofNullable(noticeRepo.findAllByUserId(userId))
                .filter(notices -> notices.isPresent() && !notices.get().isEmpty())
                .orElseGet(Optional::empty);
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
