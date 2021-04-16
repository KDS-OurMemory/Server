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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;

import com.kds.ourmemory.controller.v1.user.dto.PutUserDto;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.room.Room;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@EqualsAndHashCode
@DynamicUpdate
@Entity(name = "users")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements Serializable{
    
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")	// JPARepository 에서 스네이크 표기법을 지원하지 않아 카멜로 수정
	private Long id;

    @Column(nullable = false, name="user_sns_type") // 1: 카카오, 2: 구글, 3: 네이버
    private int snsType;
    
	@Column(nullable = false, name="user_sns_id")
	private String snsId;
	
	@Column(nullable = true, name="user_push_token")
	private String pushToken;
	
	@Column(nullable = false, name="user_fcm_push_flag")
    private boolean push;
	
	@Column(nullable = true, name="user_name")
	private String name;
	
	@Column(nullable = true, name="user_birthday")
	private String birthday;
	
	@Column(nullable = false, name="user_solar_flag")
	private boolean solar;
	
	@Column(nullable = false, name="user_birthday_open_flag")
	private boolean birthdayOpen;
	
	@Column(nullable = true, name="user_role")
	private String role;
	
	@Column(nullable = false, name="reg_date")
	private Date regDate;
	
	@Column(nullable = false, name="user_used_flag")
	private boolean used;
	
	@Column(nullable = false, name="user_device_os")
	private String deviceOs;
	
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="users_rooms",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "room_id"))
	private List<Room> rooms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="users_memorys",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "memory_id"))
    private List<Memory> memorys = new ArrayList<>();
	
	public User addRoom(Room room) {
	    this.rooms = this.rooms==null? new ArrayList<>() : this.rooms;
	    this.rooms.add(room);
	    return this;
	}
	
	public Optional<User> addRooms(List<Room> rooms) {
	    this.rooms = this.rooms==null? new ArrayList<>() : this.rooms;
	    this.rooms.addAll(rooms);
	    return Optional.of(this);
	}
	
    public User addMemory(Memory memory) {
        this.memorys = this.memorys == null? new ArrayList<>() : this.memorys;
        this.memorys.add(memory);
        return this;
    }
    
    public Optional<User> addMemorys(List<Memory> memorys) {
        this.memorys = this.memorys == null? new ArrayList<>() : this.memorys;
        this.memorys.addAll(memorys);
        return Optional.of(this);
    }
    
    public void changePushToken(String pushToken) {
        this.pushToken = StringUtils.isNotBlank(pushToken)? pushToken: this.pushToken;
    }
    
    public void updateUser(PutUserDto.Request request) {
        Optional.ofNullable(request)
            .ifPresent(req -> {
                Optional.ofNullable(request.getName()).ifPresent(name -> this.name = name);
                Optional.ofNullable(request.getBirthday()).ifPresent(birthday -> this.birthday = birthday);
                Optional.ofNullable(request.getBirthdayOpen()).ifPresent(birthdayOpen -> this.birthdayOpen = birthdayOpen);
                Optional.ofNullable(request.getPush()).ifPresent(push -> this.push = push);
            });
    }
}
