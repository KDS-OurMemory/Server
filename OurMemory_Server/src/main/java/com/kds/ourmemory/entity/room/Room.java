package com.kds.ourmemory.entity.room;

import com.kds.ourmemory.controller.v1.room.dto.UpdateRoomDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.user.User;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString(exclude = {"users", "memories"})
@DynamicUpdate
@Entity(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity implements Serializable{

	@Serial
	private static final long serialVersionUID = 2L;

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
	
	public void addUser(User user) {
	    this.users = this.users==null? new ArrayList<>() : this.users;
	    this.users.add(user);
	}
	
    public void addMemory(Memory memory) {
        this.memories = this.memories==null? new ArrayList<>() : this.memories;
        this.memories.add(memory);
    }

    public Room patchOwner(User user) {
		this.owner = user;
		return this;
	}

	public Optional<Room> updateRoom(UpdateRoomDto.Request request) {
		return Optional.ofNullable(request)
				.map(req -> {
					name = Objects.nonNull(req.getName()) ? req.getName() : name;
					opened = Objects.nonNull(req.getOpened()) ? req.getOpened() : opened;

					return this;
				});
	}

	public Room deleteRoom() {
		used = false;
		return this;
	}

	public Room deleteMemory(Memory memory) {
		memories.stream().filter(m -> m.equals(memory)).forEach(Memory::deleteMemory);
		return this;
	}
}
