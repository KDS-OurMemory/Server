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
public class Users_And_Memorys_PK implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Users users;
	private Memorys memorys;
}
