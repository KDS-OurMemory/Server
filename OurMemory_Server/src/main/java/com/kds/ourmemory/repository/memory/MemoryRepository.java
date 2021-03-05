package com.kds.ourmemory.repository.memory;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kds.ourmemory.domain.memory.Memorys;

@Transactional
public interface MemoryRepository extends JpaRepository<Memorys, Long>{
}
