package com.kds.ourmemory.entity.user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.room.Room;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@DynamicUpdate
@Entity(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity implements Serializable {
    
	/**
     * Default Serial Id
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
	
	@Column(name="user_push_token")
	private String pushToken;
	
	@Column(nullable = false, name="user_fcm_push_flag")
    private boolean push;
	
	@Column(name="user_name")
	private String name;
	
	@Column(name="user_birthday")
	private String birthday;
	
	@Column(nullable = false, name="user_solar_flag")
	private boolean solar;
	
	@Column(nullable = false, name="user_birthday_open_flag")
	private boolean birthdayOpen;
	
	@Column(name="user_role")
	private String role;
	
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
    @JoinTable(name="users_memories",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "memory_id"))
    private List<Memory> memories = new ArrayList<>();
	
	@Builder
    public User(Long id, int snsType, String snsId, String pushToken, boolean push, String name, String birthday,
            boolean solar, boolean birthdayOpen, String role, boolean used, String deviceOs) {
	    checkArgument(1 <= snsType && snsType <= 3, "지원하지 않는 SNS 인증방식입니다. 카카오(1), 구글(2), 네이버(3) 중에 입력해주시기 바랍니다.");
        checkArgument(StringUtils.isNoneBlank(snsId), "SNS ID 는 빈 값이 될 수 없습니다.");
        
	    checkNotNull(name, "이름이 입력되지 않았습니다. 이름을 입력해주세요.");
        checkArgument(StringUtils.isNoneBlank(name), "이름은 빈 값이 될 수 없습니다.");
        
        this.id = id;
        this.snsType = snsType;
        this.snsId = snsId;
        this.pushToken = pushToken;
        this.push = push;
        this.name = name;
        this.birthday = birthday;
        this.solar = solar;
        this.birthdayOpen = solar;
        this.role = role;
        this.used = used;
        this.deviceOs = deviceOs;
    }
	
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
        this.memories = this.memories == null? new ArrayList<>() : this.memories;
        this.memories.add(memory);
        return this;
    }
    
    public Optional<User> addMemories(List<Memory> memories) {
        this.memories = this.memories == null? new ArrayList<>() : this.memories;
        this.memories.addAll(memories);
        return Optional.of(this);
    }
    
    public void changePushToken(String pushToken) {
        this.pushToken = StringUtils.isNotBlank(pushToken)? pushToken: this.pushToken;
    }
    
    public void updateUser(PutUserDto.Request request) {
        Optional.ofNullable(request)
            .ifPresent(req -> {
                name = Objects.nonNull(request.getName())? request.getName() :  name;
                birthday = Objects.nonNull(request.getBirthday())? request.getBirthday() : birthday;
                birthdayOpen = Objects.nonNull(request.getBirthdayOpen())? request.getBirthdayOpen() : birthdayOpen;
                push = Objects.nonNull(request.getPush())? request.getPush() : push;
            });
    }
}
