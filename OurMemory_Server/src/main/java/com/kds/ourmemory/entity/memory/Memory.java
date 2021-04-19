package com.kds.ourmemory.entity.memory;

import static com.google.common.base.Preconditions.checkNotNull;

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

import org.hibernate.annotations.DynamicUpdate;

import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@DynamicUpdate
@Entity(name = "memorys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memory implements Serializable {
	/**
     * Default Serial Id
     */
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "memory_id")
	private Long id;
    
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "memory_writer", foreignKey = @ForeignKey(name = "memory_writer"))
    private User writer;
	
	@Column(nullable = false, name="memory_name")
	private String name;
	
	@Column(nullable = true, name="memory_contents")
	private String contents;
	
	@Column(nullable = true, name="memory_place")
	private String place;
	
	@Column(nullable = false, name="memory_start_date")
	private Date startDate;
	
	@Column(nullable = false, name="memory_end_date")
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
	
	@Builder
    public Memory(Long id, User writer, String name, String contents, String place, Date startDate, Date endDate, String bgColor,
            Date firstAlarm, Date secondAlarm, Date regDate, Date modDate, boolean used) {
	    checkNotNull(writer, "사용자 번호에 맞는 일정 작성자 정보가 없습니다. 일정 작성자 번호를 확인해주세요.");
        checkNotNull(name, "일정 제목이 입력되지 않았습니다. 일정 제목을 입력해주세요.");
        
        checkNotNull(startDate, "일정 시작시간이 입력되지 않았습니다. 일정 시작시간을 입력해주세요.");
        checkNotNull(endDate, "일정 종료시간이 입력되지 않았습니다. 일정 종료시간을 입력해주세요.");
        
        checkNotNull(bgColor, "일정 배경색이 지정되지 않았습니다. 배경색을 지정해주세요.");
        
        this.id = id;
        this.writer = writer;
        this.name = name;
        this.contents = contents;
        this.place = place;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bgColor = bgColor;
        this.firstAlarm = firstAlarm;
        this.secondAlarm = secondAlarm;
        this.regDate = regDate;
        this.modDate = modDate;
        this.used = used;
    }
    
    public Memory addRoom(Room room) {
        this.rooms = this.rooms==null? new ArrayList<>() : this.rooms;
        this.rooms.add(room);
        return this;
    }
    
    public Optional<Memory> addRooms(List<Room> rooms) {
        this.rooms = this.rooms==null? new ArrayList<>() : this.rooms;
        this.rooms.addAll(rooms);
        return Optional.of(this);
    }
    
    public Memory addUser(User user) {
        this.users = this.users==null? new ArrayList<>() : this.users;
        this.users.add(user);
        return this;
    }
    
    public Optional<Memory> addUsers(List<User> users) {
        this.users = this.users==null? new ArrayList<>() : this.users;
        this.users.addAll(users);
        return Optional.of(this);
    }
}
