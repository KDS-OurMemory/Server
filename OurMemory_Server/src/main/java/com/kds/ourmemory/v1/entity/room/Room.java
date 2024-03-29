package com.kds.ourmemory.v1.entity.room;

import com.kds.ourmemory.v1.controller.room.dto.RoomReqDto;
import com.kds.ourmemory.v1.entity.BaseTimeEntity;
import com.kds.ourmemory.v1.entity.memory.Memory;
import com.kds.ourmemory.v1.entity.user.User;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
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

	@Column(nullable = false, name="room_used_flag")
	private boolean used;

	@Column(nullable = false, name="room_opened_flag")
	private boolean opened;

	@ToString.Exclude
	@ManyToMany(mappedBy = "rooms", fetch = FetchType.LAZY)
	private List<User> users = new ArrayList<>();

	@ToString.Exclude
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="rooms_memories",
                joinColumns = @JoinColumn(name = "room_id"),
                inverseJoinColumns = @JoinColumn(name = "memory_id"))
    private List<Memory> memories = new ArrayList<>();

	@Builder
	public Room(Long id, User owner, String name, boolean opened) {
        checkNotNull(owner, "사용자 번호에 맞는 방 생성자의 정보가 없습니다. 방 생성자의 번호를 확인해주세요.");
        checkNotNull(name, "방 이름이 입력되지 않았습니다. 방 이름을 입력해주세요.");
        
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.opened = opened;
		this.used = true;
	}
	
	public void addUser(User user) {
	    this.users = this.users==null? new ArrayList<>() : this.users;
	    this.users.add(user);
	}
	
    public void addMemory(Memory memory) {
        this.memories = this.memories==null? new ArrayList<>() : this.memories;
        this.memories.add(memory);
    }

    public void recommendOwner(User user) {
		this.owner = user;
	}

	public Optional<Room> updateRoom(RoomReqDto reqDto) {
		return Optional.ofNullable(reqDto)
				.map(req -> {
					name = Objects.nonNull(req.getName()) ? req.getName() : name;
					opened = req.isOpened();

					return this;
				});
	}

	public Room deleteRoom() {
		used = false;
		return this;
	}

	public void deleteMemory(Memory memory) {
		memories.remove(memory);
	}

	public void deleteUser(User user) {
		users.remove(user);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		Room room = (Room) o;

		return Objects.equals(id, room.id);
	}

	@Override
	public int hashCode() {
		return 1140760324;
	}
}
