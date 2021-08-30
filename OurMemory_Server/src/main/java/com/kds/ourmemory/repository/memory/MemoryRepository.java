package com.kds.ourmemory.repository.memory;

import com.kds.ourmemory.entity.memory.Memory;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface MemoryRepository extends JpaRepository<Memory, Long> {
    Optional<List<Memory>> findAllByName(String name);
    Optional<List<Memory>> findAllByWriterId(Long userId);
}
