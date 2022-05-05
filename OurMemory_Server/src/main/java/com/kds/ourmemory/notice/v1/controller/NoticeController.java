package com.kds.ourmemory.notice.v1.controller;

import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.notice.v1.controller.dto.NoticeRspDto;
import com.kds.ourmemory.notice.v1.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.common.v1.controller.ApiResult.ok;

@Api(tags = {"5. Notice"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/notices")
public class NoticeController {
    private final NoticeService noticeService;

    @ApiOperation(value = "알림 목록 조회", notes = "사용자 번호에 해당하는 알림 목록을 조회한다. 조회된 모든 알림은 읽음처리한다.")
    @GetMapping("/{userId}")
    public ApiResult<List<NoticeRspDto>> findNotices(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(noticeService.findNotices(userId, true));
    }

    @ApiOperation(value = "알림 삭제", notes = """
            전달받은 알림을 삭제 처리한다.\s
            "성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("/{noticeId}")
    public ApiResult<NoticeRspDto> delete(
            @ApiParam(value = "noticeId", required = true) @PathVariable long noticeId) {
        return ok(noticeService.delete(noticeId));
    }
}
