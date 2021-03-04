package com.kds.ourmemory.domain;

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
@IdClass(UsersAndRoomsPk.class)
public class UsersAndRooms {
	@Id
	@ManyToOne
	@JoinColumn(name="user_id")
	private Users user;
	
	@Id
	@ManyToOne
	@JoinColumn(name="room_id")
	private Rooms room;
}
