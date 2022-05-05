package com.kds.ourmemory.room.v1.repository;

import com.kds.ourmemory.room.v1.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<List<Room>> findAllByName(String name);
}
