package com.kds.ourmemory.repository.relation;

import com.kds.ourmemory.entity.relation.UserMemory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {
    Optional<UserMemory> findByMemoryIdAndUserId(Long memoryId, Long userId);
}
