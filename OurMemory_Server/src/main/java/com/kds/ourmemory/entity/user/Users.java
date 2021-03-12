package com.kds.ourmemory.entity.user;

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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.DynamicUpdate;

import com.kds.ourmemory.entity.room.Rooms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@DynamicUpdate
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Users implements Serializable{
    
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")	// JPARepository 에서 스네이크 표기법을 지원하지 않아 카멜로 수정
	private Long id;
	
	@Column(nullable = false, name="user_sns_id")
	private String snsId;
	
	@Column(nullable = false, name="user_sns_type")	// 1: 카카오, 2: 구글, 3: 네이버
	private int snsType;
	
	@Column(nullable = true, name="user_push_token")
	private String pushToken;
	
	@Column(nullable = true, name="user_name")
	private String name;
	
	@Column(nullable = true, name="user_birthday")
	private String birthday;
	
	@Column(nullable = false, name="user_solar_flag")
	private boolean isSolar;
	
	@Column(nullable = false, name="user_birthday_open_flag")
	private boolean isBirthdayOpen;
	
	@Column(nullable = true, name="user_role")
	private String role;
	
	@Column(nullable = false, name="reg_date")
	private Date regDate;
	
	@Column(nullable = false, name="user_used_flag")
	private boolean used;
	
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="users_rooms",
                joinColumns = @JoinColumn(name="user_id"),
                inverseJoinColumns = @JoinColumn(name = "room_id"))
	private List<Rooms> rooms = new ArrayList<>();
	
	public Optional<Users> setRooms(List<Rooms> rooms) {
	    this.rooms = rooms;
	    return Optional.of(this);
	}
	
	public Optional<Users> addRoom(Rooms room) {
	    Optional.ofNullable(this.rooms).orElseGet(() -> this.rooms = new ArrayList<>());
	    this.rooms.add(room);
	    return Optional.of(this);
	}
	
	public Optional<Users> addRooms(List<Rooms> rooms) {
	    Optional.ofNullable(this.rooms).orElseGet(() -> this.rooms = new ArrayList<>());
	    this.rooms.addAll(rooms);
	    return Optional.of(this);
	}
}
