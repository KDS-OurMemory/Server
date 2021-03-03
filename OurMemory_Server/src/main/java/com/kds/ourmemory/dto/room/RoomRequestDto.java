package com.kds.ourmemory.dto.room;

import java.util.Date;

import com.kds.ourmemory.domain.Rooms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequestDto {
    private String name;
    private String owner;
    private boolean opened;
    
    private Long[] member;
    
    public Rooms toEntity() {
        return Rooms.builder()
                .name(name)
                .owner(owner)
                .regDate(new Date())
                .opened(opened)
                .used(true)
                .build();
    }
}
