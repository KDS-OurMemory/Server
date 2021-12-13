package com.kds.ourmemory.v1.repository.room;

import com.kds.ourmemory.v1.entity.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<List<Room>> findAllByName(String name);
}
