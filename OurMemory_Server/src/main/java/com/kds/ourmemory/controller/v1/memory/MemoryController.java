package com.kds.ourmemory.controller.v1.memory;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.memory.dto.DeleteMemoryResponseDto;
import com.kds.ourmemory.controller.v1.memory.dto.FindMemoryResponseDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.InsertMemoryResponseDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.service.v1.memory.MemoryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@Api(tags = {"3. Memory"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class MemoryController {
    
    private final MemoryService memoryService;

    @ApiOperation(value="일정 추가", notes = "일정을 추가하고 일정-방-사용자 의 관계를 설정한다.")
    @PostMapping("/memory")
    public ApiResult<InsertMemoryResponseDto> addMemory(@RequestBody InsertMemoryRequestDto request) {
        return ok(memoryService.insert(request));
    }
    
    @ApiOperation(value = "일정 조회", notes = "사용자가 생성했거나 참여중인 일정을 조회한다.")
    @GetMapping("/memory/{userId}")
    public ApiResult<List<FindMemoryResponseDto>> findMemorys(@ApiParam(value = "userId", required = true) @PathVariable Long userId) {
        return ok(memoryService.findMemorys(userId).stream().filter(Memory::isUsed).map(FindMemoryResponseDto::new)
                .collect(Collectors.toList()));
    }
    
    @ApiOperation(value = "일정 삭제", notes = "일정 삭제, 사용자-일정-방 연결된 관계 삭제")
    @DeleteMapping("/memory/{memoryId}")
    public ApiResult<DeleteMemoryResponseDto> deleteMemory(@ApiParam(value = "memoryId", required = true) @PathVariable Long memoryId) {
        return ok(memoryService.delete(memoryId));
    }
}
