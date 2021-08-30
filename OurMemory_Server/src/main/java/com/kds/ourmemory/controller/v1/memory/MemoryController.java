package com.kds.ourmemory.controller.v1.memory;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.memory.dto.*;
import com.kds.ourmemory.entity.relation.AttendanceStatus;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"4. Memory"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/memories")
public class MemoryController {
    private final MemoryService memoryService;

    @ApiOperation(value = "일정 추가", notes = "일정을 개인방에 추가한 뒤 추가할 방에 공유한다. 추가/공유 둘다 동일한 기능으로 일정-방 관계데이터를 만들어준다.")
    @PostMapping
    public ApiResult<InsertMemoryDto.Response> insert(@RequestBody InsertMemoryDto.Request request) {
        return ok(memoryService.insert(request));
    }

    @ApiOperation(value = "일정 개별 조회")
    @GetMapping("/{memoryId}")
    public ApiResult<FindMemoryDto.Response> findMemory(
            @PathVariable long memoryId) {
        return ok(memoryService.find(memoryId));
    }

    @ApiOperation(value = "일정 목록 조회", notes = "조건에 맞는 일정을 검색한다.")
    @GetMapping
    public ApiResult<List<FindMemoriesDto.Response>> findMemories(
            @ApiParam(value = "일정 작성자 번호") @RequestParam(required = false) Long writerId,
            @ApiParam(value = "일정 제목") @RequestParam(required = false) String name
    ) {
        return ok(memoryService.findMemories(writerId, name));
    }

    @ApiOperation(value = "일정 수정", notes = "전달받은 값이 있는 경우 수정")
    @PutMapping("/{memoryId}")
    public ApiResult<UpdateMemoryDto.Response> update(
            @PathVariable long memoryId,
            @RequestParam UpdateMemoryDto.Request request
    ) {
        return ok(memoryService.update(memoryId, request));
    }

    @ApiOperation(value = "일정 참석 여부 설정", notes = "일정에 참석/불참 여부 설정, 사용자-일정 관계 테이블에 레코드를 추가하는 방식으로 설정함.")
    @PostMapping("/{memoryId}/attendance/{userId}/{status}")
    public ApiResult<AttendMemoryDto.Response> setAttend(
            @PathVariable long memoryId,
            @PathVariable long userId,
            @PathVariable AttendanceStatus status
            ) {
        return ok(memoryService.setAttendanceStatus(memoryId, userId, status));
    }

    @ApiOperation(value = "일정 삭제", notes = "일정 삭제 처리")
    @DeleteMapping("/{memoryId}")
    public ApiResult<DeleteMemoryDto.Response> delete(
            @PathVariable long memoryId) {
        return ok(memoryService.delete(memoryId));
    }
}
