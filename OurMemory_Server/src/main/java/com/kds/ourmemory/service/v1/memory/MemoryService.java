package com.kds.ourmemory.service.v1.memory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.IntFunction;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.domain.Memorys;
import com.kds.ourmemory.dto.memory.MemoryResponseDto;
import com.kds.ourmemory.repository.memory.MemoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemoryService {
    private final MemoryRepository repository;
    
    public MemoryResponseDto addMemory(Memorys memory) {
        Memorys saveMemory = repository.save(memory);
        
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String today = format.format(new Date());
        
        IntFunction<MemoryResponseDto> response = code -> new MemoryResponseDto(code, today);
        
        return Optional.ofNullable(saveMemory)
                .map(s -> response.apply(0))
                .orElseGet(() -> response.apply(1));
    }
}
