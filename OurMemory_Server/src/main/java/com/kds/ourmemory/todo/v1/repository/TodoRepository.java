package com.kds.ourmemory.todo.v1.repository;

import com.kds.ourmemory.todo.v1.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface TodoRepository extends JpaRepository<Todo, Long> {
    Optional<List<Todo>> findAllByWriterId(Long userId);
}
