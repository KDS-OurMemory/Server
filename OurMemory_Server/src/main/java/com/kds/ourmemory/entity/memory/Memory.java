package com.kds.ourmemory.entity.memory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "memorys")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Memory implements Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "memory_id")
	private Long id;
    
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(foreignKey = @ForeignKey(name = "memory_writer"))
    private User user;
	
	@Column(nullable = false, name="memory_name")
	private String name;
	
	@Column(nullable = true, name="memory_contents")
	private String contents;
	
	@Column(nullable = true, name="memory_place")
	private String place;
	
	@Column(nullable = true, name="memory_start_date")
	private Date startDate;
	
	@Column(nullable = true, name="memory_end_date")
	private Date endDate;
	
	@Column(nullable = false, name="memory_bg_color")
	private String bgColor;
	
	@Column(nullable = true, name="memory_first_alarm")
	private Date firstAlarm;
	
	@Column(nullable = true, name="memory_second_alarm")
	private Date secondAlarm;
	
	@Column(nullable = false, name="reg_date")
	private Date regDate;
	
	@Column(nullable = true, name="mod_date")
	private Date modDate;
	
	@Column(nullable = false, name="memory_used")
	private boolean used;
	
	@ManyToMany(mappedBy = "memorys", fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();
	
	@ManyToMany(mappedBy = "memorys", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();
	
	public Optional<Memory> setRooms(List<Room> rooms) {
        this.rooms = rooms;
        return Optional.of(this);
    }
    
    public Memory addRoom(Room room) {
        Optional.ofNullable(this.rooms).orElseGet(() -> this.rooms = new ArrayList<>());
        this.rooms.add(room);
        return this;
    }
    
    public Optional<Memory> addRooms(List<Room> rooms) {
        Optional.ofNullable(this.rooms).orElseGet(() -> this.rooms = new ArrayList<>());
        this.rooms.addAll(rooms);
        return Optional.of(this);
    }
    
    public Optional<Memory> setUsers(List<User> users) {
        this.users = users;
        return Optional.of(this);
    }
    
    public Memory addUser(User user) {
        Optional.ofNullable(this.users).orElseGet(() -> this.users = new ArrayList<>());
        this.users.add(user);
        return this;
    }
    
    public Optional<Memory> addUsers(List<User> users) {
        Optional.ofNullable(this.users).orElseGet(() -> this.users = new ArrayList<>());
        this.users.addAll(users);
        return Optional.of(this);
    }
}
