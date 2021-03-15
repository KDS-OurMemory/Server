package com.kds.ourmemory.service.v1.memory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryResponseDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.repository.memory.MemoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemoryService {
    private final MemoryRepository memoryRepo;
    
    public MemoryResponseDto addMemory(Memory memory) throws CMemoryException{
        return insert(memory).map(m -> {
            DateFormat format = new SimpleDateFormat("yyyyMMdd");
            String today = format.format(new Date());
            
            return new MemoryResponseDto(today);
        }).orElseThrow(() -> new CMemoryException("Add Memory to "));
    }
    
    private Optional<Memory> insert(Memory memory) {
        return Optional.of(memoryRepo.save(memory));
    }
}
