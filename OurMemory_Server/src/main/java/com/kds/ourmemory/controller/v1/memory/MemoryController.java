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
    @GetMapping("/{memoryId}/room/{roomId}")
    public ApiResult<FindMemoryDto.Response> findMemory(
            @PathVariable long memoryId,
            @PathVariable long roomId
    ) {
        return ok(memoryService.find(memoryId, roomId));
    }

    @ApiOperation(value = "일정 목록 조회", notes = "조건에 맞는 일정을 검색한다. 일정 시작시간 -> 일정 생성시간 순으로 정렬된다.")
    @GetMapping
    public ApiResult<List<FindMemoriesDto.Response>> findMemories(
            @ApiParam(value = "일정 작성자 번호") @RequestParam(required = false) Long writerId,
            @ApiParam(value = "일정 제목") @RequestParam(required = false) String name
    ) {
        return ok(memoryService.findMemories(writerId, name));
    }

    @ApiOperation(value = "일정 수정", notes = "전달받은 값이 있는 경우 수정")
    @PutMapping("/{memoryId}/writer/{userId}")
    public ApiResult<UpdateMemoryDto.Response> update(
            @PathVariable long memoryId,
            @PathVariable long userId,
            @RequestBody UpdateMemoryDto.Request request
    ) {
        return ok(memoryService.update(memoryId, userId, request));
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

    @ApiOperation(value = "일정 공유",
            notes = """
                    사용자가 대상 목록에게 일정을 공유시킨다.\s
                    1. 사용자 개별 공유: 각 사용자 별로 방 생성 뒤 일정 공유,\s
                    2. 사용자 그룹 공유: 사용자들을 참여자로 방 생성 후 일정 공유,\s
                    3. 기존 방에 공유: 전달받은 각각의 방에 일정 공유"""
    )
    @PostMapping("/{memoryId}/share/{userId}")
    public ApiResult<ShareMemoryDto.Response> shareMemory(
            @PathVariable long memoryId,
            @PathVariable long userId,
            @RequestBody ShareMemoryDto.Request request
    ) {
        return ok(memoryService.shareMemory(memoryId, userId, request));
    }

    @ApiOperation(value = "일정 삭제", notes = """
            일정을 방에서 삭제처리한다. 아래의 경우에 따라 처리 방식이 나뉜다.\s
            1. 공유방에서 삭제 -> 일정-방 관계 삭제\s
            2. 개인방에서 삭제 -> 일정 삭제 처리""")
    @DeleteMapping("/{memoryId}")
    public ApiResult<DeleteMemoryDto.Response> delete(
            @PathVariable long memoryId,
            @RequestBody DeleteMemoryDto.Request request) {
        return ok(memoryService.delete(memoryId, request));
    }
}
