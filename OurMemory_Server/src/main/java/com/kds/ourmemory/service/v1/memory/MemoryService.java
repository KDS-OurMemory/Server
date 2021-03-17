package com.kds.ourmemory.service.v1.memory;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryRequestDto;
import com.kds.ourmemory.controller.v1.memory.dto.MemoryResponseDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.repository.memory.MemoryRepository;
import com.kds.ourmemory.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemoryService {
    private final MemoryRepository memoryRepo;
    private final UserRepository userRepo;
    
    public MemoryResponseDto addMemory(MemoryRequestDto request) throws CMemoryException{
        return userRepo.findBySnsId(request.getSnsId())
                .map(user -> Memory.builder()
                        .writerId(user.getId())
                        .name(request.getName())
                        .contents(request.getContents())
                        .place(request.getPlace())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .firstAlarm(request.getFirstAlarm())
                        .secondAlarm(request.getSecondAlarm())
                        .bgColor(request.getBgColor())
                        .regDate(new Date())
                        .used(true)
                        .build()
                        ).map(memory -> insert(memory))
                .map(m -> {
                    return new MemoryResponseDto(currentDate());
                }).orElseThrow(() -> new CMemoryException("Add Memory to DB Failed."));
    }
    
    private Optional<Memory> insert(Memory memory) {
        return Optional.of(memoryRepo.save(memory));
    }
}
