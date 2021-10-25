package com.kds.ourmemory.entity.todolist;

import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

@DynamicUpdate
@Entity(name = "todolist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todolist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todolist_id")
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "todolist_writer", foreignKey = @ForeignKey(name = "todolist_writer"))
    private User writer;

    @Column(nullable = false, name="todolist_contents")
    private String contents;

    @Column(nullable = false, name="todolist_todo_date")
    private LocalDateTime todoDate;

    @Column(nullable = false, name="todolist_used_flag", columnDefinition = "boolean not null comment '0: 사용안함, 1: 사용'")
    private boolean used;

    @Builder
    public Todolist(Long id, User writer, String contents, LocalDateTime todoDate, boolean used) {
        checkNotNull(writer, "사용자 번호에 맞는 TODO 리스트 작성자 정보가 없습니다. 작성자 번호를 확인해주세요.");
        checkNotNull(contents, "TODO 내용이 없습니다. 내용을 입력해주세요.");
        checkNotNull(todoDate, "TODO 목표 날짜가 없습니다. 날짜를 입력해주세요.");

        this.id = id;
        this.writer = writer;
        this.contents = contents;
        this.todoDate = todoDate;
        this.used = used;
    }
}
