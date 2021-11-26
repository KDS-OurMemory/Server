package com.kds.ourmemory.entity.todo;

import com.kds.ourmemory.controller.v1.todo.dto.TodoReqDto;
import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@DynamicUpdate
@Entity(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "todo_writer", foreignKey = @ForeignKey(name = "todo_writer"))
    private User writer;

    @Column(nullable = false, name="todo_contents")
    private String contents;

    @Column(nullable = false, name="todo_date")
    private LocalDateTime todoDate;

    @Column(nullable = false, name="todo_used_flag", columnDefinition = "boolean not null comment '0: 사용안함, 1: 사용'")
    private boolean used;

    @Builder
    public Todo(Long id, User writer, String contents, LocalDateTime todoDate) {
        checkNotNull(writer, "사용자 번호에 맞는 TODO 작성자 정보가 없습니다. 작성자 번호를 확인해주세요.");
        checkNotNull(contents, "TODO 내용이 없습니다. 내용을 입력해주세요.");
        checkNotNull(todoDate, "TODO 목표 날짜가 없습니다. 날짜를 입력해주세요.");

        this.id = id;
        this.writer = writer;
        this.contents = contents;
        this.todoDate = todoDate;
        this.used = true;
    }

    public Optional<Todo> updateTodo(TodoReqDto reqDto) {
        return Optional.ofNullable(reqDto)
                .map(req -> {
                    this.contents = StringUtils.isNoneBlank(req.getContents())? req.getContents() : this.contents;
                    this.todoDate = req.getTodoDate() != null? req.getTodoDate().atStartOfDay() : this.todoDate;

                    return this;
                });
    }

    public void deleteTodo() {
        this.used = false;
    }
}
