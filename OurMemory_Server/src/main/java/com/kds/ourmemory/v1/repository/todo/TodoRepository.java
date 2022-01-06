package com.kds.ourmemory.v1.repository.todo;

import com.kds.ourmemory.v1.entity.todo.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface TodoRepository extends JpaRepository<Todo, Long> {
    Optional<List<Todo>> findAllByWriterId(Long userId);
}
