package com.kds.ourmemory.controller.v1.memory;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.controller.v1.memory.dto.MemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryResponseDto;
import com.kds.ourmemory.service.v1.memory.MemoryService;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class MemoryController {
    
    private final MemoryService memoryService;

    @ApiOperation(value="일정 추가", notes = "앱에서 전달받은 데이터로 일정 추가")
    @PostMapping("/memory")
    public MemoryResponseDto addMemory(@RequestBody MemoryRequestDto request) {
        return memoryService.addMemory(request.toEntity());
    }
}
