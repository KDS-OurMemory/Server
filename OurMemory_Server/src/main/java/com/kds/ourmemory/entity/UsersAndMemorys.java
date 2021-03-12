package com.kds.ourmemory.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.kds.ourmemory.entity.memory.Memorys;
import com.kds.ourmemory.entity.user.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(UsersAndMemorysPk.class)
public class UsersAndMemorys {
    @Id
    @ManyToOne
    @JoinColumn(name="user_id")
    private Users users;
    
    @Id
    @ManyToOne
    @JoinColumn(name="memory_id")
    private Memorys memorys;
	
	@Column(nullable = false, name="user_memory_owner")
	private boolean owned;
}
