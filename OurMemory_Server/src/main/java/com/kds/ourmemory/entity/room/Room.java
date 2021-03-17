package com.kds.ourmemory.entity.room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.DynamicUpdate;

import com.kds.ourmemory.entity.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@DynamicUpdate
@Entity(name = "rooms")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Room implements Serializable{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Long id;
	
	@Column(nullable = false, name="room_owner")
	private Long owner;
	
	@Column(nullable = false, name="room_name")
	private String name;
	
	@Column(nullable = false, name="reg_date")
	private Date regDate;
	
	@Column(nullable = false, name="room_used")
	private boolean used;
	
	@Column(nullable = false, name="room_opened")
	private boolean opened;
	
	@ManyToMany(mappedBy = "rooms", fetch = FetchType.LAZY)
	private List<User> users = new ArrayList<>();
	
	public Optional<Room> setUsers(List<User> users) {
	    this.users = users;
	    return Optional.of(this);
	}
	
	public Room addUser(User user) {
	    Optional.ofNullable(this.users).orElseGet(() -> this.users = new ArrayList<>());
	    this.users.add(user);
	    return this;
	}
	
	public Optional<Room> addUsers(List<User> users) {
	    Optional.ofNullable(this.users).orElseGet(() -> this.users = new ArrayList<>());
	    this.users.addAll(users);
	    return Optional.of(this);
    }	
}
