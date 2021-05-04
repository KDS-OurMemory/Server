package com.kds.ourmemory.entity.room;

import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString(exclude = {"users", "memories"})
@DynamicUpdate
@Entity(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity implements Serializable{

	/**
     * Default Serial Id
     */
	private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Long id;
	
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "room_owner", foreignKey = @ForeignKey(name = "room_owner"))
	private User owner;
	
	@Column(nullable = false, name="room_name")
	private String name;
	
	@Column(nullable = false, name="room_used")
	private boolean used;
	
	@Column(nullable = false, name="room_opened")
	private boolean opened;
	
	@ManyToMany(mappedBy = "rooms", fetch = FetchType.LAZY)
	private List<User> users = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="rooms_memories",
                joinColumns = @JoinColumn(name = "room_id"),
                inverseJoinColumns = @JoinColumn(name = "memory_id"))
    private List<Memory> memories = new ArrayList<>();
	
	@Builder
	public Room(Long id, User owner, String name, boolean used, boolean opened) {
        checkNotNull(owner, "사용자 번호에 맞는 방 생성자의 정보가 없습니다. 방 생성자의 번호를 확인해주세요.");
        checkNotNull(name, "방 이름이 입력되지 않았습니다. 방 이름을 입력해주세요.");
        
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.used = used;
        this.opened = opened;
	}
	
	public Optional<Room> setUsers(List<User> users) {
	    this.users = users;
	    return Optional.of(this);
	}
	
	public Room addUser(User user) {
	    this.users = this.users==null? new ArrayList<>() : this.users;
	    this.users.add(user);
	    return this;
	}
	
	public Optional<Room> addUsers(List<User> users) {
	    this.users = this.users==null? new ArrayList<>() : this.users;
	    this.users.addAll(users);
	    return Optional.of(this);
    }
	
    public Optional<Room> setMemories(List<Memory> memories) {
        this.memories = memories;
        return Optional.of(this);
    }

    public Room addMemory(Memory memory) {
        this.memories = this.memories==null? new ArrayList<>() : this.memories;
        this.memories.add(memory);
        return this;
    }

    public Optional<Room> addMemories(List<Memory> memories) {
        this.memories = this.memories==null? new ArrayList<>() : this.memories;
        this.memories.addAll(memories);
        return Optional.of(this);
    }
}
