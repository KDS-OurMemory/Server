package com.kds.ourmemory.controller.v1.notice;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.notice.dto.DeleteNoticeDto;
import com.kds.ourmemory.controller.v1.notice.dto.FindNoticesDto;
import com.kds.ourmemory.controller.v1.notice.dto.InsertNoticeDto;
import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.service.v1.notice.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"5. Notice"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class NoticeController {
    private final NoticeService noticeService;

    @ApiOperation(value = "알림 추가")
    @PostMapping("/notice")
    public ApiResult<InsertNoticeDto.Response> insert(@RequestBody InsertNoticeDto.Request request) {
        return ok(noticeService.insert(request));
    }

    @ApiOperation(value = "알림 조회", notes = "사용자 번호에 해당하는 알림 목록을 조회한다.")
    @GetMapping("/notices/{userId}")
    public ApiResult<List<FindNoticesDto.Response>> findNotices(
            @ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(noticeService.findNotices(userId).stream()
                .filter(Notice::getUsed)
                .map(FindNoticesDto.Response::new)
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "알림 삭제")
    @DeleteMapping("/notice/{noticeId}")
    public ApiResult<DeleteNoticeDto.Response> deleteNotice(
            @ApiParam(value = "noticeId", required = true) @PathVariable long noticeId) {
        return ok(noticeService.deleteNotice(noticeId));
    }
}
