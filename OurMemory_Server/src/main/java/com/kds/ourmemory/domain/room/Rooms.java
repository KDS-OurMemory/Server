package com.kds.ourmemory.domain.room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.kds.ourmemory.domain.user.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Rooms implements Serializable{

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
	
	@ManyToMany
	@JoinTable(name="users_rooms",
	            joinColumns = @JoinColumn(name="user_id"),
	            inverseJoinColumns = @JoinColumn(name = "room_id"))
	private List<Users> users = new ArrayList<>();
	
	public Optional<Rooms> setUsers(List<Users> users) {
	    this.users = users;
	    return Optional.of(this);
	}
	
	public Optional<Rooms> addUser(Users user) {
	    Optional.ofNullable(this.users).orElseGet(() -> this.users = new ArrayList<>());
	    this.users.add(user);
	    return Optional.of(this);
	}
	
	public Optional<Rooms> addUsers(List<Users> users) {
	    Optional.ofNullable(this.users).orElseGet(() -> this.users = new ArrayList<>());
	    this.users.addAll(users);
	    return Optional.of(this);
    }	
}
