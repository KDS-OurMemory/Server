package com.kds.ourmemory.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Users_And_Rooms_PK implements Serializable{
	/**
	 * 복합키를 설정하기 위해 Serializeable 추가
	 */
	private static final long serialVersionUID = 1L;
	
	private Users users;
	private Rooms rooms;
}
