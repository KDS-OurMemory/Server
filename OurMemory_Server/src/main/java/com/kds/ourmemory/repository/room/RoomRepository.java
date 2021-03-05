package com.kds.ourmemory.repository.room;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kds.ourmemory.domain.Rooms;

@Transactional
public interface RoomRepository extends JpaRepository<Rooms, Long>{
}
