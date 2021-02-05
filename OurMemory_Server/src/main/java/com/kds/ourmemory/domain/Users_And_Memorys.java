package com.kds.ourmemory.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(Users_And_Memorys_PK.class)
public class Users_And_Memorys {
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
