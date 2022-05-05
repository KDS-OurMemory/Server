package com.kds.ourmemory.relation.v1.reopsitory;

import com.kds.ourmemory.memory.v1.entity.Memory;
import com.kds.ourmemory.relation.v1.entity.UserMemory;
import com.kds.ourmemory.user.v1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {
    Optional<UserMemory> findByMemoryIdAndUserId(Long memoryId, Long userId);
    Optional<List<UserMemory>> findAllByMemoryAndUserIn(Memory memory, List<User> users);
}
