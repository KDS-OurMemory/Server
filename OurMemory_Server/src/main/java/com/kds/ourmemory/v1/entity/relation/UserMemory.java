package com.kds.ourmemory.v1.entity.relation;

import com.kds.ourmemory.v1.entity.memory.Memory;
import com.kds.ourmemory.v1.entity.user.User;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
@DynamicUpdate
@Entity(name = "users_memories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_memory_id")
    private Long id;

    @Comment("ATTEND: 참석, ABSENCE: 불참")
    @Column(nullable = false, name = "user_memory_attendance_status")
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "memory_id")
    private Memory memory;

    @Builder
    public UserMemory(Long id, AttendanceStatus status, User user, Memory memory) {
        checkNotNull(status, "참석 상태값이 없습니다. 참석 상태값을 설정해 주세요.");
        checkNotNull(user, "사용자 정보가 없습니다. 사용자 정보를 입력해주세요.");
        checkNotNull(memory, "일정 정보가 없습니다. 일정 정보를 입력해주세요.");

        this.id = id;
        this.status = status;
        this.user = user;
        this.memory = memory;
    }

    public void updateAttendance(AttendanceStatus status) {
        this.status = status;
    }
}
