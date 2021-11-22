package com.kds.ourmemory.controller.v1.notice;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.notice.dto.NoticeDto;
import com.kds.ourmemory.service.v1.notice.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"5. Notice"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/notices")
public class NoticeController {
    private final NoticeService noticeService;

    @ApiOperation(value = "알림 목록 조회", notes = "사용자 번호에 해당하는 알림 목록을 조회한다. 조회된 모든 알림은 읽음처리한다.")
    @GetMapping("/{userId}")
    public ApiResult<List<NoticeDto>> findNotices(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(noticeService.findNotices(userId, true));
    }

    @ApiOperation(value = "알림 삭제", notes = "전달받은 알림을 삭제 처리한다.")
    @DeleteMapping("/{noticeId}")
    public ApiResult<NoticeDto> delete(
            @ApiParam(value = "noticeId", required = true) @PathVariable long noticeId) {
        return ok(noticeService.deleteNotice(noticeId));
    }
}
