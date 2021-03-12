package com.kds.ourmemory.entity;

import java.io.Serializable;

import com.kds.ourmemory.entity.memory.Memorys;
import com.kds.ourmemory.entity.user.Users;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
class UsersAndMemorysPk implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Users users;
    private Memorys memorys;
}