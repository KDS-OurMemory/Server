package com.kds.ourmemory.controller.v1.room.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.kds.ourmemory.entity.room.Rooms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequestDto {
    private String name;
    private Long owner;
    private boolean opened;
    
    private List<Long> member;
    
    public Rooms toEntity() {
        return Rooms.builder()
                .name(name)
                .owner(owner)
                .regDate(new Date())
                .opened(opened)
                .used(true)
                .users(new ArrayList<>())
                .build();
    }
}
