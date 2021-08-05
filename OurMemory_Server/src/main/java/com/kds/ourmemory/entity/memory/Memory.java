package com.kds.ourmemory.entity.memory;

import com.kds.ourmemory.controller.v1.memory.dto.UpdateMemoryDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
@DynamicUpdate
@Entity(name = "memories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memory extends BaseTimeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 3L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "memory_id")
	private Long id;
    
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "memory_writer", foreignKey = @ForeignKey(name = "memory_writer"))
    private User writer;
	
	@Column(nullable = false, name="memory_name")
	private String name;
	
	@Column(name="memory_contents")
	private String contents;
	
	@Column(name="memory_place")
	private String place;
	
	@Column(nullable = false, name="memory_start_date")
	private LocalDateTime startDate;
	
	@Column(nullable = false, name="memory_end_date")
	private LocalDateTime endDate;
	
	@Column(nullable = false, name="memory_bg_color")
	private String bgColor;
	
	@Column(name="memory_first_alarm")
	private LocalDateTime firstAlarm;
	
	@Column(name="memory_second_alarm")
	private LocalDateTime secondAlarm;
	
	@Column(nullable = false, name="memory_used_flag", columnDefinition = "boolean not null comment '0: 사용안함, 1: 사용'")
	private boolean used;

    @ToString.Exclude
	@ManyToMany(mappedBy = "memories", fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();

    @ToString.Exclude
	@ManyToMany(mappedBy = "memories", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();
	
	@Builder
    public Memory(Long id, User writer, String name, String contents, String place,
                  LocalDateTime startDate, LocalDateTime endDate, String bgColor,
            LocalDateTime firstAlarm, LocalDateTime secondAlarm, boolean used
    ) {
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
        this.used = used;
    }
    
    public void addRoom(Room room) {
        this.rooms = this.rooms==null? new ArrayList<>() : this.rooms;
        this.rooms.add(room);
    }

    public void addUser(User user) {
        this.users = this.users==null? new ArrayList<>() : this.users;
        this.users.add(user);
    }

    public Optional<Memory> updateMemory(UpdateMemoryDto.Request request) {
        return Optional.ofNullable(request)
                .map(req -> {
                    Optional.ofNullable(req.getName()).ifPresent(reqName -> this.name = reqName);
                    Optional.ofNullable(req.getContents()).ifPresent(reqContents -> this.contents = reqContents);
                    Optional.ofNullable(req.getPlace()).ifPresent(reqPlace -> this.place = reqPlace);
                    Optional.ofNullable(req.getStartDate()).ifPresent(reqStartDate -> this.startDate = reqStartDate);
                    Optional.ofNullable(req.getEndDate()).ifPresent(reqEndDate -> this.endDate = reqEndDate);
                    Optional.ofNullable(req.getFirstAlarm()).ifPresent(reqFirstAlarm -> this.firstAlarm = reqFirstAlarm);
                    Optional.ofNullable(req.getSecondAlarm()).ifPresent(reqSecondAlarm -> this.secondAlarm = reqSecondAlarm);
                    Optional.ofNullable(req.getBgColor()).ifPresent(reqBgColor -> this.bgColor = reqBgColor);

                    return this;
                });
    }

    public Memory deleteMemory() {
	    this.used = false;
	    return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Memory memory = (Memory) o;

        return Objects.equals(id, memory.id);
    }

    @Override
    public int hashCode() {
        return 1289417505;
    }
}
