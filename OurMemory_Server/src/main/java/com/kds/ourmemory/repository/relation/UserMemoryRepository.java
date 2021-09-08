package com.kds.ourmemory.repository.relation;

import com.kds.ourmemory.entity.memory.Memory;
import com.kds.ourmemory.entity.relation.UserMemory;
import com.kds.ourmemory.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {
    Optional<UserMemory> findByMemoryIdAndUserId(Long memoryId, Long userId);
    Optional<List<UserMemory>> findAllByMemoryAndUserIn(Memory memory, List<User> users);
}
