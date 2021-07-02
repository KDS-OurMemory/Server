package com.kds.ourmemory.repository.room;

import com.kds.ourmemory.entity.room.Room;
import com.kds.ourmemory.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<List<Room>> findAllByOwnerOrName(User user, String name);
}
