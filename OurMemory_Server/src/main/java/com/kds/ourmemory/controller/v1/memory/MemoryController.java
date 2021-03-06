package com.kds.ourmemory.controller.v1.memory;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.memory.dto.*;
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

    @ApiOperation(value = "일정 추가", notes = "일정을 추가하고 일정-방-사용자 의 관계를 설정한다.")
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
            @ApiParam(value = "사용자 번호, 일정 작성자 혹은 참여자") @RequestParam(required = false) Long userId,
            @ApiParam(value = "일정 제목") @RequestParam(required = false) String name
    ) {
        return ok(memoryService.findMemories(userId, name));
    }

    @ApiOperation(value = "일정 수정", notes = "전달받은 값이 있는 경우 수정")
    @PutMapping("/{memoryId}")
    public ApiResult<UpdateMemoryDto.Response> update(
            @PathVariable long memoryId,
            @RequestParam UpdateMemoryDto.Request request
    ) {
        return ok(memoryService.update(memoryId, request));
    }

    @ApiOperation(value = "일정 삭제", notes = "일정 삭제 처리")
    @DeleteMapping("/{memoryId}")
    public ApiResult<DeleteMemoryDto.Response> delete(
            @PathVariable long memoryId) {
        return ok(memoryService.delete(memoryId));
    }
}
