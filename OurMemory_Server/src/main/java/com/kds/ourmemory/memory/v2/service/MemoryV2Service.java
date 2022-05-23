package com.kds.ourmemory.memory.v2.service;

import com.kds.ourmemory.memory.v1.service.MemoryService;
import com.kds.ourmemory.memory.v2.controller.dto.MemoryFindRspDto;
import com.kds.ourmemory.memory.v2.controller.dto.MemoryInsertReqDto;
import com.kds.ourmemory.memory.v2.controller.dto.MemoryInsertRspDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemoryV2Service {

    private final MemoryService memoryService;

    public MemoryInsertRspDto insert(MemoryInsertReqDto reqDto) {
        return new MemoryInsertRspDto(memoryService.insert(reqDto.toDto()));
    }

    public MemoryFindRspDto find(long memoryId, long roomId) {
        return new MemoryFindRspDto(memoryService.find(memoryId, roomId));
    }

}
