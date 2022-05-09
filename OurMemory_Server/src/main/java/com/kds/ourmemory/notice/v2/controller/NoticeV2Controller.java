package com.kds.ourmemory.notice.v2.controller;

import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.notice.v1.controller.dto.NoticeRspDto;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeDeleteRspDto;
import com.kds.ourmemory.notice.v2.controller.dto.NoticeFindNoticesRspDto;
import com.kds.ourmemory.notice.v2.service.NoticeV2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.common.v1.controller.ApiResult.ok;

@Api(tags = {"5-2. Notice"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v2/notices")
public class NoticeV2Controller {

    private final NoticeV2Service noticeV2Service;

    @ApiOperation(value = "알림 목록 조회", notes = "사용자 번호에 해당하는 알림 목록을 조회한다. 조회된 모든 알림은 읽음처리한다.")
    @GetMapping("/{userId}")
    public ApiResult<List<NoticeFindNoticesRspDto>> findNotices(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(noticeV2Service.findNotices(userId, true));
    }

    @ApiOperation(value = "알림 삭제", notes = """
            전달받은 알림을 삭제 처리한다.\s
            "성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("/{noticeId}")
    public ApiResult<NoticeDeleteRspDto> delete(
            @ApiParam(value = "noticeId", required = true) @PathVariable long noticeId) {
        return ok(noticeV2Service.delete(noticeId));
    }

}
