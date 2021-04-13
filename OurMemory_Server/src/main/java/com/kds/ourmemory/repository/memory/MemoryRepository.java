package com.kds.ourmemory.repository.memory;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kds.ourmemory.entity.memory.Memory;

@Transactional
public interface MemoryRepository extends JpaRepository<Memory, Long> {
}
