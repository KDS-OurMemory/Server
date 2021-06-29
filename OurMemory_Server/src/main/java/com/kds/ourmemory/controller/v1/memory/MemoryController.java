package com.kds.ourmemory.controller.v1.memory;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryDto;
import com.kds.ourmemory.controller.v1.memory.dto.FindMemoriesDto;
import com.kds.ourmemory.controller.v1.memory.dto.FindMemoryDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
            @ApiParam(value = "memoryId", required = true) @PathVariable long memoryId) {
        return ok(memoryService.find(memoryId));
    }

    @ApiOperation(value = "개인 일정 목록 조회", notes = "사용자가 생성했거나 참여중인 일정을 조회한다.")
    @GetMapping
    public ApiResult<List<FindMemoriesDto.Response>> findMemories(
            @ApiParam(value = "userId", required = true) @RequestParam Long userId) {
        return ok(memoryService.findMemories(userId).stream()
                .filter(Memory::isUsed)
                .sorted(Comparator.comparing(Memory::getStartDate)) // first order
                .sorted(Comparator.comparing(Memory::getEndDate))   // second order
                .map(FindMemoriesDto.Response::new)
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "일정 삭제", notes = "일정 삭제, 사용자-일정-방 연결된 관계 삭제")
    @DeleteMapping("/{memoryId}")
    public ApiResult<DeleteMemoryDto.Response> delete(
            @ApiParam(value = "memoryId", required = true) @PathVariable long memoryId) {
        return ok(memoryService.delete(memoryId));
    }
}
