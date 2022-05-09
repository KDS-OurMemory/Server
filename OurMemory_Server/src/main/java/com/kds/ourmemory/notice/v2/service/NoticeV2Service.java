package com.kds.ourmemory.notice.v2.service;

import com.kds.ourmemory.notice.v1.service.NoticeService;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeDeleteRspDto;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeFindNoticesRspDto;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeInsertReqDto;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeInsertRspDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NoticeV2Service {

    private final NoticeService noticeService;

    public NoticeInsertRspDto insert(NoticeInsertReqDto reqDto) {
        return new NoticeInsertRspDto(noticeService.insert(reqDto.toDto()));
    }

    public List<NoticeFindNoticesRspDto> findNotices(long userId, boolean isReadProcessing) {
        return noticeService.findNotices(userId, isReadProcessing).stream().map(NoticeFindNoticesRspDto::new).toList();
    }

    public NoticeDeleteRspDto delete(long noticeId) {
        return new NoticeDeleteRspDto(noticeService.delete(noticeId));
    }

}
